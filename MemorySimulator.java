import java.util.*;

public class MemorySimulator {
    // Constants
    private static final int PAGE_SIZE = 256;   // Bytes per page
    private static final int NUM_PAGES = 16;    // Total number of pages
    private static final int TLB_SIZE = 4;      // TLB entries
    private static final int CACHE_SIZE = 8;    // Cache entries
    private static final int MEMORY_SIZE = NUM_PAGES * PAGE_SIZE;

    // Data structures
    private static LinkedHashMap<Integer, Integer> tlb = new LinkedHashMap<>(TLB_SIZE, 0.75f, false);
    private static LinkedHashMap<Integer, Integer> pageTable = new LinkedHashMap<>();
    private static LinkedHashMap<Integer, String> cache = new LinkedHashMap<>(CACHE_SIZE, 0.75f, false);
    private static String[] mainMemory = new String[NUM_PAGES];

    // Initialize main memory with data
    static {
        for (int i = 0; i < NUM_PAGES; i++) {
            mainMemory[i] = "Data_" + i;
        }
    }

    // Helper to get page number and offset
    private static int[] getPageNumberAndOffset(int virtualAddress) {
        int pageNumber = virtualAddress / PAGE_SIZE;
        int offset = virtualAddress % PAGE_SIZE;
        return new int[]{pageNumber, offset};
    }

    // Access memory simulation
    private static String accessMemory(int virtualAddress) {
        int[] pageOffset = getPageNumberAndOffset(virtualAddress);
        int pageNumber = pageOffset[0];
        int offset = pageOffset[1];

        System.out.println("\nAccessing Virtual Address: " + virtualAddress +
                           " (Page " + pageNumber + ", Offset " + offset + ")");

        int frameNumber;

        // 1. TLB Lookup
        if (tlb.containsKey(pageNumber)) {
            frameNumber = tlb.get(pageNumber);
            System.out.println("TLB Hit: Page " + pageNumber + " -> Frame " + frameNumber);
        } else {
            System.out.println("TLB Miss");

            // 2. Page Table Lookup
            if (pageTable.containsKey(pageNumber)) {
                frameNumber = pageTable.get(pageNumber);
                System.out.println("Page Table Hit: Page " + pageNumber + " -> Frame " + frameNumber);
            } else {
                // 3. Page Fault
                frameNumber = pageTable.size() % NUM_PAGES;
                pageTable.put(pageNumber, frameNumber);
                System.out.println("Page Fault: Loaded Page " + pageNumber + " into Frame " + frameNumber);
            }

            // Update TLB
            if (tlb.size() >= TLB_SIZE) {
                int oldestPage = tlb.keySet().iterator().next();
                tlb.remove(oldestPage);
            }
            tlb.put(pageNumber, frameNumber);
        }

        // 4. Cache Lookup
        if (cache.containsKey(frameNumber)) {
            System.out.println("Cache Hit: Frame " + frameNumber);
        } else {
            System.out.println("Cache Miss: Loading Frame " + frameNumber + " into Cache");
            if (cache.size() >= CACHE_SIZE) {
                int oldestFrame = cache.keySet().iterator().next();
                cache.remove(oldestFrame);
            }
            cache.put(frameNumber, mainMemory[frameNumber]);
        }

        return cache.get(frameNumber);
    }

    public static void main(String[] args) {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            int virtualAddress = rand.nextInt(MEMORY_SIZE);
            String data = accessMemory(virtualAddress);
            System.out.println("Data at address " + virtualAddress + ": " + data);
        }
    }
}
