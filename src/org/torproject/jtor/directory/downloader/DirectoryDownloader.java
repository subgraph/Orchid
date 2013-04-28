package org.torproject.jtor.directory.downloader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.torproject.jtor.CircuitManager;
import org.torproject.jtor.ConsensusDocument;
import org.torproject.jtor.Directory;
import org.torproject.jtor.crypto.TorRandom;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.Timestamp;
import org.torproject.jtor.directory.impl.DocumentParserFactoryImpl;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;

public class DirectoryDownloader implements Runnable {

	private final Thread thread;
	private final Directory directory;
	private final CircuitManager circuitManager;
	private final DocumentParserFactory parserFactory;
	private final DescriptorProcessor descriptorProcessor;
	private final TorRandom random;
	private final Executor executor = Executors.newCachedThreadPool();

	private volatile boolean isDownloadingCertificates;
	private volatile boolean isDownloadingConsensus;
	private final AtomicInteger outstandingDescriptorTasks;

	private ConsensusDocument currentConsensus;
	private Date consensusDownloadTime;

	public DirectoryDownloader(Directory directory,
			CircuitManager circuitManager) {
		this.directory = directory;
		this.circuitManager = circuitManager;
		this.parserFactory = new DocumentParserFactoryImpl();
		this.descriptorProcessor = new DescriptorProcessor(directory);
		this.outstandingDescriptorTasks = new AtomicInteger();
		this.thread = new Thread(this);
		this.random = new TorRandom();
	}

	public void start() {
		setCurrentConsensus(directory.getCurrentConsensusDocument());
		thread.start();
	}

	public void run() {
		directory.loadFromStore();
		while (true) {
			checkCertificates();
			checkConsensus();
			checkDescriptors();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	Directory getDirectory() {
		return directory;
	}

	CircuitManager getCircuitManager() {
		return circuitManager;
	}

	DocumentParserFactory getDocumentParserFactory() {
		return parserFactory;
	}

	void clearDownloadingCertificates() {
		isDownloadingCertificates = false;
	}

	void clearDownloadingConsensus() {
		isDownloadingConsensus = false;
	}

	void clearDownloadingDescriptors() {
		outstandingDescriptorTasks.decrementAndGet();
	}

	private void checkCertificates() {
		if (isDownloadingCertificates
				|| directory.getRequiredCertificates().isEmpty()) {
			return;
		}

		List<HexDigest> fps = new ArrayList<HexDigest>(
				directory.getRequiredCertificates());
		CertificateDownloadTask task = new CertificateDownloadTask(fps, this);
		isDownloadingCertificates = true;
		executor.execute(task);
	}

	void setCurrentConsensus(ConsensusDocument consensus) {
		if (consensus != null) {
			currentConsensus = consensus;
			consensusDownloadTime = chooseDownloadTimeForConsensus(consensus);
		} else {
			currentConsensus = null;
			consensusDownloadTime = null;
		}
	}

	/*
	 * dir-spec 5.1: Downloading network-status documents
	 * 
	 *   To avoid swarming the caches whenever a consensus expires, the clients
	 *   download new consensuses at a randomly chosen time after the caches are
	 *   expected to have a fresh consensus, but before their consensus will
	 *   expire. (This time is chosen uniformly at random from the interval
	 *   between the time 3/4 into the first interval after the consensus is no
	 *   longer fresh, and 7/8 of the time remaining after that before the
	 *   consensus is invalid.)
	 * 
	 *   [For example, if a cache has a consensus that became valid at 1:00, and
	 *   is fresh until 2:00, and expires at 4:00, that cache will fetch a new
	 *   consensus at a random time between 2:45 and 3:50, since 3/4 of the
	 *   one-hour interval is 45 minutes, and 7/8 of the remaining 75 minutes is
	 *   65 minutes.]
	 */
	private Date chooseDownloadTimeForConsensus(ConsensusDocument consensus) {
		final long va = getMilliseconds(consensus.getValidAfterTime());
		final long fu = getMilliseconds(consensus.getFreshUntilTime());
		final long vu = getMilliseconds(consensus.getValidUntilTime());
		final long i1 = fu - va;
		final long start = fu + ((i1 * 3) / 4);
		final long i2 = ((vu - start) * 7) / 8;
		final long r = random.nextLong(i2);
		final long download = start + r;
		return new Date(download);
	}

	private boolean needConsensusDownload() {
		if (currentConsensus == null || !currentConsensus.isLive()) {
			return true;
		}
		return consensusDownloadTime.before(new Date());
	}

	private long getMilliseconds(Timestamp ts) {
		return ts.getDate().getTime();
	}

	private void checkConsensus() {
		if (isDownloadingConsensus || !needConsensusDownload()) {
			return;
		}
		ConsensusDownloadTask task = new ConsensusDownloadTask(this);
		isDownloadingConsensus = true;
		executor.execute(task);
	}

	private void checkDescriptors() {
		if (outstandingDescriptorTasks.get() > 0) {
			return;
		}
		List<List<HexDigest>> ds = descriptorProcessor
				.getDescriptorDigestsToDownload();
		if (ds.isEmpty()) {
			return;
		}
		for (List<HexDigest> dlist : ds) {
			DescriptorDownloadTask task = new DescriptorDownloadTask(dlist,
					this);
			outstandingDescriptorTasks.incrementAndGet();
			executor.execute(task);
		}
	}
}
