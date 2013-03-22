package org.torproject.jtor.directory.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.ConsensusDocument;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.impl.DocumentParserFactoryImpl;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;

public class DirectoryDownloader implements Runnable {
	
	private final Thread thread;
	private final Directory directory;
	private final CircuitManager circuitManager;
	private final DocumentParserFactory parserFactory;
	private final DescriptorProcessor descriptorProcessor;
	private final Executor executor = Executors.newCachedThreadPool();
	
	private volatile boolean isDownloadingCertificates;
	private volatile boolean isDownloadingConsensus;
	private final AtomicInteger outstandingDescriptorTasks;
	
	public DirectoryDownloader(Directory directory, CircuitManager circuitManager) {
		this.directory = directory;
		this.circuitManager = circuitManager;
		this.parserFactory = new DocumentParserFactoryImpl();
		this.descriptorProcessor = new DescriptorProcessor(directory);
		this.outstandingDescriptorTasks = new AtomicInteger();
		this.thread = new Thread(this);
	}
	
	public void start() {
		thread.start();
	}

	public void run() {
		while(true) {
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
		if(isDownloadingCertificates || directory.getRequiredCertificates().isEmpty()) {
			return;
		}
		
		List<HexDigest> fps = new ArrayList<HexDigest>(directory.getRequiredCertificates());
		CertificateDownloadTask task = new CertificateDownloadTask(fps, this);
		isDownloadingCertificates = true;
		executor.execute(task);
	}
	
	
	private void checkConsensus() {
		final ConsensusDocument consensus = directory.getCurrentConsensusDocument();
		if(isDownloadingConsensus || consensus != null && consensus.isLive()) {
			return;
		}
		ConsensusDownloadTask task = new ConsensusDownloadTask(this);
		isDownloadingConsensus = true;
		executor.execute(task);
	}
	
	private void checkDescriptors() {
		if(outstandingDescriptorTasks.get() > 0) {
			return;
		}
		List<List<HexDigest>> ds = descriptorProcessor.getDescriptorDigestsToDownload();
		if(ds.isEmpty()) {
			return;
		}
		for(List<HexDigest> dlist: ds) {
			DescriptorDownloadTask task = new DescriptorDownloadTask(dlist, this);
			outstandingDescriptorTasks.incrementAndGet();
			executor.execute(task);
		}
	}
}
