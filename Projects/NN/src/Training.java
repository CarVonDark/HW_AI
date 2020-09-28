import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Training {
	
	private static double[][] trainingDataSet;
	private static double[][] desiredOutput;
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

		FeedForwardNetwork n = new FeedForwardNetwork(nRows*nCols, nRows, 1, 1);
		n.initNetwork(trainingDataSet, desiredOutput, 0.2, 1);
		n.trainNetwork(1000, true);
		n.printWeights();
		n.testNetwork();
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
        desiredOutput = new double[numberOfLabels][1];
        
        for(int i = 0; i < numberOfLabels; i++) {
        	desiredOutput[i][0] = labelInputStream.readUnsignedByte();
            for (int r = 0; r < nRows; r++) {
                for (int c = 0; c < nCols; c++) {
                    trainingDataSet[i][r*nCols+c] = dataInputStream.readUnsignedByte();
                }
            }
            
        }
	}

}