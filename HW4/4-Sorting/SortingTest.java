import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class SortingTest {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            boolean isRandom = false;    // 입력받은 배열이 난수인가 아닌가?
            int[] value;    // 입력 받을 숫자들의 배열
            String nums = br.readLine();    // 첫 줄을 입력 받음
            if (nums.charAt(0) == 'r') {
                // 난수일 경우
                isRandom = true;    // 난수임을 표시

                String[] nums_arg = nums.split(" ");

                int numsize = Integer.parseInt(nums_arg[1]);    // 총 갯수
                int rminimum = Integer.parseInt(nums_arg[2]);    // 최소값
                int rmaximum = Integer.parseInt(nums_arg[3]);    // 최대값

                Random rand = new Random();    // 난수 인스턴스를 생성한다.

                value = new int[numsize];    // 배열을 생성한다.
                for (int i = 0; i < value.length; i++)    // 각각의 배열에 난수를 생성하여 대입
                    value[i] = rand.nextInt(rmaximum - rminimum + 1) + rminimum;
            } else {
                // 난수가 아닐 경우
                int numsize = Integer.parseInt(nums);

                value = new int[numsize];    // 배열을 생성한다.
                for (int i = 0; i < value.length; i++)    // 한줄씩 입력받아 배열원소로 대입
                    value[i] = Integer.parseInt(br.readLine());
            }

            // 숫자 입력을 다 받았으므로 정렬 방법을 받아 그에 맞는 정렬을 수행한다.
            while (true) {
                int[] newvalue = value.clone();    // 원래 값의 보호를 위해 복사본을 생성한다.

                String command = br.readLine();

                long t = System.currentTimeMillis();
                switch (command.charAt(0)) {
                    case 'B':    // Bubble Sort
                        newvalue = DoBubbleSort(newvalue);
                        break;
                    case 'I':    // Insertion Sort
                        newvalue = DoInsertionSort(newvalue);
                        break;
                    case 'H':    // Heap Sort
                        newvalue = DoHeapSort(newvalue);
                        break;
                    case 'M':    // Merge Sort
                        newvalue = DoMergeSort(newvalue);
                        break;
                    case 'Q':    // Quick Sort
                        newvalue = DoQuickSort(newvalue);
                        break;
                    case 'R':    // Radix Sort
                        newvalue = DoRadixSort(newvalue);
                        break;
                    case 'X':
                        return;    // 프로그램을 종료한다.
                    default:
                        throw new IOException("잘못된 정렬 방법을 입력했습니다.");
                }
                if (isRandom) {
                    // 난수일 경우 수행시간을 출력한다.
                    System.out.println((System.currentTimeMillis() - t) + " ms");
                } else {
                    // 난수가 아닐 경우 정렬된 결과값을 출력한다.
                    for (int i = 0; i < newvalue.length; i++) {
                        System.out.println(newvalue[i]);
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoBubbleSort(int[] value) {
        // TODO : Bubble Sort 를 구현하라.
        // value는 정렬안된 숫자들의 배열이며 value.length 는 배열의 크기가 된다.
        // 결과로 정렬된 배열은 리턴해 주어야 하며, 두가지 방법이 있으므로 잘 생각해서 사용할것.
        // 주어진 value 배열에서 안의 값만을 바꾸고 value를 다시 리턴하거나
        // 같은 크기의 새로운 배열을 만들어 그 배열을 리턴할 수도 있다.
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value.length - i - 1; j++) {
                if (value[j] > value[j + 1]) {
                    int tmp = value[j];
                    value[j] = value[j + 1];
                    value[j + 1] = tmp;
                }
            }
        }

        return (value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoInsertionSort(int[] value) {
        // TODO : Insertion Sort 를 구현하라.
        for (int i = 1; i < value.length; i++) {
            int pivot = value[i];
            int j;
            // pivot보다 큰 모든 요소를 오른쪽으로 이동
            for (j = i - 1; j >= 0 && value[j] > pivot; j--) {
                value[j + 1] = value[j];
            }
            // 남은 자리에 pivot 삽입
            value[j + 1] = pivot;
        }
        return (value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoHeapSort(int[] value) {
        // TODO : Heap Sort 를 구현하라.
        for (int i = value.length / 2; i >= 0; i--) {
            heapify(value, i, value.length);
        }
        for (int i = value.length - 1; i > 0; i--) {
            int tmp = value[0];
            value[0] = value[i];
            value[i] = tmp;
            heapify(value, 0, i);
        }
        return (value);
    }

    private static void heapify(int[] arr, int i, int size) {
        int lefti = 2 * i + 1; // left child
        int righti = 2 * i + 2; // right child
        int largeri = i;
        if (lefti < size && arr[lefti] > arr[largeri])
            largeri = lefti;
        if (righti < size && arr[righti] > arr[largeri])
            largeri = righti;
        if (largeri != i) {
            int tmp = arr[i];
            arr[i] = arr[largeri];
            arr[largeri] = tmp;
            heapify(arr, largeri, size);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoMergeSort(int[] value) {
        // TODO : Merge Sort 를 구현하라.
        mergeSort(value, 0, value.length - 1);
        return value;
    }

    // sort value[low..high]
    private static void mergeSort(int[] value, int p, int r) {
        if (r <= p) return;

        int q = (p + r) / 2;
        mergeSort(value, p, q);
        mergeSort(value, q + 1, r);
        merge(value, p, q, r);
    }

    // merge value[p..q], (q..r]
    private static void merge(int[] value, int p, int q, int r) {
        // split 된원본 보존
        int[] left = new int[q - p + 1];
        int[] right = new int[r - q];
        System.arraycopy(value, p, left, 0, left.length);
        System.arraycopy(value, q + 1, right, 0, right.length);

        int pi = 0; // 왼쪽 배열 인덱스
        int qi = 0; // 오른쪽 배열 인덱스

        // Start merging
        for (int writer = p; writer <= r; writer++) {
            if (pi < left.length && qi < right.length) {
                if (left[pi] < right[qi]) {
                    value[writer] = left[pi];
                    pi++;
                } else {
                    value[writer] = right[qi];
                    qi++;
                }
            } else if (pi < left.length) {
                // 남은 것 복사
                value[writer] = left[pi];
                pi++;
            } else if (qi < right.length) {
                // 남은 것 복사
                value[writer] = right[qi];
                qi++;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoQuickSort(int[] value) {
        // TODO : Quick Sort 를 구현하라.
        quickSort(value, 0, value.length);
        return (value);
    }

    // quick sort value[p..q)
    private static void quickSort(int[] value, int p, int q) {
//        System.out.println("Quicksort called");
        if (q - p <= 1)
            return;
        int pivot = value[p];
        int lastPart1 = p;
        for (int i = p + 1; i < q; i++) {
            if (value[i] < pivot) {
                lastPart1++;
                int tmp = value[lastPart1];
                value[lastPart1] = value[i];
                value[i] = tmp;
            }
        }
        value[p] = value[lastPart1];
        value[lastPart1] = pivot;
        quickSort(value, p, lastPart1);
        quickSort(value, lastPart1 + 1, q);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoRadixSort(int[] value) {
        // TODO : Radix Sort 를 구현하라.
        // 256진법 radix sort
        // 크기를 4바이트로 가정하므로 max, min을 구할 필요가 없다.

        int[] currentBuffer = value;
        int[] backBuffer = value.clone();
        
        // 음수를 처리하기 위해 양수는 최상위비트를 1로 음수는 0이 되게 반전
        for (int i = 0; i < value.length; i++) {
            value[i] ^= 0x80000000;
        }
        for (int i = 0; i < 4; i++) {
            int[] counts = new int[256];
            // 끝에서 i째 바이트에 대한 count정렬을 위한 count
            for (int j = 0; j < currentBuffer.length; j++) {
                int theByte = (currentBuffer[j] >> (i << 3)) & 0x00FF; // 끝에서 i째 바이트를 가져온다.
//                System.out.println("origValue: " + currentBuffer[j] + "theByte:" + theByte);
                counts[theByte]++;
            }
            // count를 누적 count로 변환.
            for (int j = 1; j < 256; j++) {
                counts[j] = counts[j] + counts[j - 1];
            }
            // 누적 count를 이용하여 데이터를 정렬하여 넣는다.
            for (int j = currentBuffer.length - 1; j >= 0; j--) {
                int theByte = (currentBuffer[j] >> (i << 3)) & 0x00FF; // 끝에서 i째 바이트를 가져온다.
                backBuffer[counts[theByte] - 1] = currentBuffer[j]; // 원본데이터를 개수에 맞게 적절한 위치에 넣는다
                counts[theByte]--;
            }
            // 잦은메모리 할당을 막기 위한 최적화.
            // write 할 곳과 read할곳을 바꾼다.
            int[] tmp = currentBuffer;
            currentBuffer = backBuffer;
            backBuffer = tmp;
        }

        // 음수 처리를 위한 처리를 복구
        for (int i = 0; i < value.length; i++) {
            currentBuffer[i] ^= 0x80000000;
        }
        return currentBuffer;
    }
}
