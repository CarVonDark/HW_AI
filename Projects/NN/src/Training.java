import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Training {
	
	private static double[][] trainingDataSet;
	private static double[][] desiredOutput;
	private static double[][] testingDataSet;
	private static double[][] desiredTestOutput;
	private static int testDataSize = 0;
	private static int magicNumber = 0;
	private static int numberOfItems = 0;
	private static int nRows = 0;
	private static int nCols = 0;

	public static void main(String[] args) {
		
		try {
			readData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FeedForwardNetwork n = new FeedForwardNetwork(nRows*nCols, 100, 1, 10);
		n.initNetwork(trainingDataSet, desiredOutput, 0.001, 1);
		n.trainNetwork(200, true);
		n.printWeights();
		n.testNetworkBatch(10000, testingDataSet,desiredTestOutput,true);
	}
	
	public static void readData() throws IOException{
		DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream("images.idx3-ubyte")));
        magicNumber = dataInputStream.readInt();
        numberOfItems = dataInputStream.readInt();
        nRows = dataInputStream.readInt();
        nCols = dataInputStream.readInt();

        System.out.println("magic number is " + magicNumber);
        System.out.println("number of items is " + numberOfItems);
        System.out.println("number of rows is: " + nRows);
        System.out.println("number of cols is: " + nCols);

        DataInputStream labelInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream("labels.idx1-ubyte")));
        int labelMagicNumber = labelInputStream.readInt();
        int numberOfLabels = labelInputStream.readInt();

        System.out.println("labels magic number is: " + labelMagicNumber);
        System.out.println("number of labels is: " + numberOfLabels);
        
        int pixelNum = nRows * nCols;
        trainingDataSet = new double[numberOfItems][pixelNum];
        desiredOutput = new double[numberOfLabels][10];
        
        for(int i = 0; i < numberOfLabels; i++) {
        	int read = labelInputStream.readUnsignedByte();
        	desiredOutput[i][read] = 1;
            for (int r = 0; r < nRows; r++) {
                for (int c = 0; c < nCols; c++) {
                    double num = dataInputStream.readUnsignedByte();
                    trainingDataSet[i][r*nCols+c] = num/255; //standardize data
                }
            }
            
        }
        
        //test data
        DataInputStream testDataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream("test_images.idx3-ubyte")));
        magicNumber = testDataInputStream.readInt();
        int numberOfTestItems = testDataInputStream.readInt();
        nRows = testDataInputStream.readInt();
        nCols = testDataInputStream.readInt();

        System.out.println("magic number is " + magicNumber);
        System.out.println("number of items is " + numberOfTestItems);
        System.out.println("number of rows is: " + nRows);
        System.out.println("number of cols is: " + nCols);

        DataInputStream testlabelInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream("test_labels.idx1-ubyte")));
        labelMagicNumber = testlabelInputStream.readInt();
        int numberOfTestLabels = testlabelInputStream.readInt();

        System.out.println("labels magic number is: " + labelMagicNumber);
        System.out.println("number of labels is: " + numberOfTestLabels);
        
        pixelNum = nRows * nCols;
        testingDataSet = new double[numberOfTestItems][pixelNum];
        desiredTestOutput = new double[numberOfLabels][10];
        
        for(int i = 0; i < numberOfTestLabels; i++) {
        	int read = testlabelInputStream.readUnsignedByte();
        	desiredTestOutput[i][read] = 1;
            for (int r = 0; r < nRows; r++) {
                for (int c = 0; c < nCols; c++) {
                    double num = testDataInputStream.readUnsignedByte();
                    testingDataSet[i][r*nCols+c] = num/255;
                }
            }
            
        }
	}

}