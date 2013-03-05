package org.torproject.jtor.circuits.cells;


public interface Cell {
	/** Command constant for a PADDING type cell. */
	static final int PADDING = 0;

	/** Command constant for a CREATE type cell. */
	static final int CREATE = 1;

	/** Command constant for a CREATED type cell. */
	static final int CREATED = 2;

	/** Command constant for a RELAY type cell. */
	static final int RELAY = 3;

	/** Command constant for a DESTROY type cell. */
	static final int DESTROY = 4;

	/** Command constant for a CREATE_FAST type cell. */
	static final int CREATE_FAST = 5;

	/** Command constant for a CREATED_FAST type cell. */
	static final int CREATED_FAST = 6;

	/** Command constant for a VERSIONS type cell. */
	static final int VERSIONS = 7;

	/** Command constant for a NETINFO type cell. */
	static final int NETINFO = 8;

	/** Command constant for a RELAY_EARLY type cell. */
	static final int RELAY_EARLY = 9;

	static final int ERROR_NONE = 0;
	static final int ERROR_PROTOCOL = 1;
	static final int ERROR_INTERNAL = 2;
	static final int ERROR_REQUESTED = 3;
	static final int ERROR_HIBERNATING = 4;
	static final int ERROR_RESOURCELIMIT = 5;
	static final int ERROR_CONNECTFAILED = 6;
	static final int ERROR_OR_IDENTITY = 7;
	static final int ERROR_OR_CONN_CLOSED = 8;
	static final int ERROR_FINISHED = 9;
	static final int ERROR_TIMEOUT = 10;
	static final int ERROR_DESTROYED = 11;
	static final int ERROR_NOSUCHSERVICE = 12;


	/**
	 * The fixed size of a standard cell.
	 */
	static final int CELL_LEN = 512;

	/**
	 * The length of a standard cell header.
	 */
	static final int CELL_HEADER_LEN = 3;

	/**
	 * The header length for a variable length cell (ie: VERSIONS)
	 */
	static final int CELL_VAR_HEADER_LEN = 5;

	/**
	 * The length of the payload space in a standard cell.
	 */
	static final int CELL_PAYLOAD_LEN = CELL_LEN - CELL_HEADER_LEN;

	/**
	 * Return the circuit id field from this cell.
	 *
	 * @return The circuit id field of this cell.
	 */
	int getCircuitId();

	/**
	 * Return the command field from this cell.
	 *
	 * @return The command field of this cell.
	 */
	int getCommand();

	/**
	 * Set the internal pointer to the first byte after the cell header.
	 */
	void resetToPayload();

	/**
	 * Return the next byte from the cell and increment the internal pointer by one byte.
	 *
	 * @return The byte at the current pointer location.
	 */
	int getByte();

	/**
	 * Return the byte at the specified offset into the cell.
	 *
	 * @param index The cell offset.
	 * @return The byte at the specified offset.
	 */
	int getByteAt(int index);

	/**
	 * Return the next 16-bit big endian value from the cell and increment the internal pointer by two bytes.
	 *
	 * @return The 16-bit short value at the current pointer location.
	 */
	int getShort();

	/**
	 * Return the 16-bit big endian value at the specified offset into the cell.
	 *
	 * @param index The cell offset.
	 * @return The 16-bit short value at the specified offset.
	 */
	int getShortAt(int index);

	/**
	 * Return the next 32-bit big endian value from the cell and increment the internal pointer by four bytes.
	 *
	 * @return The 32-bit integer value at the current pointer location.
	 */
	int getInt();

	/**
	 * Copy <code>buffer.length</code> bytes from the cell into <code>buffer</code>.  The data is copied starting
	 * from the current internal pointer location and afterwards the internal pointer is incremented by <code>buffer.length</code>
	 * bytes.
	 *
	 * @param buffer The array of bytes to copy the cell data into.
	 */
	void getByteArray(byte[] buffer);

	/**
	 * Return the number of bytes already packed (for outgoing cells) or unpacked (for incoming cells).  This is
	 * equivalent to the internal pointer position.
	 *
	 * @return The number of bytes already consumed from this cell.
	 */
	int cellBytesConsumed();

	/**
	 * Return the number of bytes remaining between the current internal pointer and the end of the cell.  If fields
	 * are being added to a new cell for transmission then this value indicates the remaining space in bytes for
	 * adding new data.  If fields are being read from a received cell then this value describes the number of bytes
	 * which can be read without overflowing the cell.
	 *
	 * @return The number of payload bytes remaining in this cell.
	 */
	int cellBytesRemaining();

	/**
	 * Store a byte at the current pointer location and increment the pointer by one byte.
	 *
	 * @param value The byte value to store.
	 */
	void putByte(int value);

	/**
	 * Store a byte at the specified offset into the cell.
	 *
	 * @param index The offset in bytes into the cell.
	 * @param value The byte value to store.
	 */
	void putByteAt(int index, int value);

	/**
	 * Store a 16-bit short value in big endian order at the current pointer location and
	 * increment the pointer by two bytes.
	 *
	 * @param value The 16-bit short value to store.
	 */
	void putShort(int value);

	/**
	 * Store a 16-bit short value in big endian byte order at the specified offset into the cell
	 * and increment the pointer by two bytes.
	 *
	 * @param index The offset in bytes into the cell.
	 * @param value The 16-bit short value to store.
	 */
	void putShortAt(int index, int value);

	/**
	 * Store a 32-bit integer value in big endian order at the current pointer location and
	 * increment the pointer by 4 bytes.
	 *
	 * @param value The 32-bit integer value to store.
	 */
	void putInt(int value);

	/**
	 * Store the entire array <code>data</code> at the current pointer location and increment
	 * the pointer by <code>data.length</code> bytes.
	 *
	 * @param data The array of bytes to store in the cell.
	 */
	void putByteArray(byte[] data);

	/**
	 * Store <code>length</code> bytes of the byte array <code>data</code> starting from
	 * <code>offset</code> into the array at the current pointer location and increment
	 * the pointer by <code>length</code> bytes.
	 *
	 * @param data The source array of bytes.
	 * @param offset The offset into the source array.
	 * @param length The number of bytes from the source array to store.
	 */
	void putByteArray(byte[] data, int offset, int length);

	/**
	 * Return the entire cell data as a raw array of bytes.  For all cells except
	 * <code>VERSIONS</code>, this array will be exactly <code>CELL_LEN</code> bytes long.
	 *
	 * @return The cell data as an array of bytes.
	 */
	byte[] getCellBytes();

	void putString(String string);
}
