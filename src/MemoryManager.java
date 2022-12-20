import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class MemoryManager {

	private int myNumberOfPages;
	private int myPageSize; // In bytes
	private int myNumberOfFrames;
	private int[] myPageTable; // -1 if page is not in physical memory
	private byte[] myRAM; // physical memory RAM
	private RandomAccessFile myPageFile;
	private int myNextFreeFramePosition = 0;
	private int myNumberOfPageFaults = 0;
	private int myPageReplacementAlgorithm = 0;
	private boolean myShortOutput = false;
	private Deque<Integer> timeline;

	public MemoryManager(int numberOfPages, int pageSize, int numberOfFrames, String pageFile,
			int pageReplacementAlgorithm, boolean shortOutput) {

		myNumberOfPages = numberOfPages;
		myPageSize = pageSize;
		myNumberOfFrames = numberOfFrames;
		myPageReplacementAlgorithm = pageReplacementAlgorithm;
		myShortOutput = shortOutput;
		timeline = new ArrayDeque<>(numberOfFrames);

		initPageTable();
		myRAM = new byte[myNumberOfFrames * myPageSize];

		try {

			myPageFile = new RandomAccessFile(pageFile, "r");

		} catch (FileNotFoundException ex) {
			System.out.println("Can't open page file: " + ex.getMessage());
		}
	}

	private void initPageTable() {
		myPageTable = new int[myNumberOfPages];
		for (int n = 0; n < myNumberOfPages; n++) {
			myPageTable[n] = -1;
		}
	}

	public byte readFromMemory(int logicalAddress) {
		int pageNumber = getPageNumber(logicalAddress);
		int offset = getPageOffset(logicalAddress);

		if (myPageTable[pageNumber] == -1) {// works in all tasks 1-3, if page does not exist
			pageFault(pageNumber);
		} else if (myPageReplacementAlgorithm == Seminar3.LRU_PAGE_REPLACEMENT) {// LRU, page exists, works only in Task 3,
			// will not work when you launch Task 1 or 2
			if(timeline.removeFirstOccurrence(pageNumber))// we update the page in timeline, by finding it, removing it and adding again in the tail
				timeline.addLast(pageNumber);// adding to tail
		}

		int frame = myPageTable[pageNumber];
		int physicalAddress = frame * myPageSize + offset;
		byte data = myRAM[physicalAddress];

		if (myShortOutput != true) {
			System.out.print("Virtual address: " + logicalAddress);
		System.out.print(" Physical address: " + physicalAddress);
		System.out.println(" Value: " + data);
		}
		return data;
	}

	private int getPageNumber(int logicalAddress) {
		// Implement by student in task one
		//logical address is a particular number of a byte, and we need to find out in which page shall be put it in.

		return logicalAddress / myPageSize;// page size shows show many bytes can be addressed by one page,
		                                  // page is a collection of numbers that represent particular number of the byte of main memory
	}

	private int getPageOffset(int logicalAddress) {
		// Implement by student in task one
		// main memory consists of bytes, for example 2GB main memory (RAM) consists of 2 billion bytes,
		// so logical address shows the particular number of a byte within the launched program and
		// physical address shows the byte number inside RAM
		return logicalAddress % myPageSize;//offset means particular number of a byte within the page
	}

	// it checks which task is being tested, 0 for Task 1, 1 for Task 2 and 2 for Task 3
	private void pageFault(int pageNumber) {
		if (myPageReplacementAlgorithm == Seminar3.NO_PAGE_REPLACEMENT)
			handlePageFault(pageNumber);

		else if (myPageReplacementAlgorithm == Seminar3.FIFO_PAGE_REPLACEMENT)
			handlePageFaultFIFO(pageNumber);

		else if (myPageReplacementAlgorithm == Seminar3.LRU_PAGE_REPLACEMENT)
			handlePageFaultLRU(pageNumber);

		readFromPageFileToMemory(pageNumber);
	}

	private void readFromPageFileToMemory(int pageNumber) {
		try {
			int frame = myPageTable[pageNumber];
			myPageFile.seek(pageNumber * myPageSize);
			for (int b = 0; b < myPageSize; b++)
				myRAM[frame * myPageSize + b] = myPageFile.readByte();
		} catch (IOException ex) {

		}
	}

	public int getNumberOfPageFaults() {
		return myNumberOfPageFaults;
	}

	private void handlePageFault(int pageNumber) {
		// Implement by student in task one
		// This is the simple case where we assume same size of physical and logical memory
		// myNextFreeFramePosition is used to point to next free frame position
		myNumberOfPageFaults++;
		myPageTable[pageNumber] = myNextFreeFramePosition++;
	}

	private void handlePageFaultFIFO(int pageNumber) {
		// Implement by student in task two
		// this solution allows different size of physical and logical memory page replacement using FIFO
		// Note depending on your solution, you might need to change parts of the supplied code, this is allowed.
		myNumberOfPageFaults++;
		
		if (myNextFreeFramePosition == myNumberOfFrames) {//if there are no free frames (space for new pages) more, start swapping old pages out
			int oldestPage = timeline.poll();//remove from head and store in 'oldestPage' variable
			int frame = myPageTable[oldestPage];  // getting frame of the oldest page
			myPageTable[pageNumber] = frame; // we set frame to new page
			myPageTable[oldestPage] = -1; // if page not exist in main memory then we indicate to -1 and its pf
			timeline.addLast(pageNumber);

		} else {// still having free space for new page
			myPageTable[pageNumber] = myNextFreeFramePosition++;// myNextFreeFramePos points to next available frame,
			timeline.addLast(pageNumber);
		}
		// putting new page in the tail, tracking pages oldness, older in head, newest in tail -> 5 (head), 9, 2, 3 (tail)
	}

	private void handlePageFaultLRU(int pageNumber) {
		// Implement by student in task three
		// this solution allows different size of physical and logical memory page replacement using LRU
		// Note depending on your solution, you might need to change parts of the supplied code, this is allowed.
		handlePageFaultFIFO(pageNumber);
	}
}
