import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author adas
 */
public class Prir {

	private int[] indices;
	Matrix C;
	
	public static void main(String[] args) {
		Prir prir = new Prir();
		prir.start(args);
	}
	
	private void start(String[] args){
		
		check_arg_count(args);
		
		Matrix A = null;
		Matrix B = null;
		try{
			A = readFile(args[0]);
			print(A);
			B = readFile(args[1]);
			print(B);
		}catch(FileNotFoundException e){
			System.out.println("Nie udało się otworzyć pliku.");
			System.exit(-1);
		}
	   
		int nThreads = Integer.parseInt(args[2]);
		prepareIndices(nThreads, A, B);
		mul(A, B, nThreads);
		print(C);
	}
	
	private void check_arg_count(String[] args){
		
		if(args.length < 3){
			System.out.println("Za mała liczba argumentów.");
			System.exit(-1);
		}
		
	}
	
	private Matrix readFile(String fname) throws FileNotFoundException {
		File f = new File(fname);
		Scanner scanner = new Scanner(f).useLocale(Locale.ENGLISH);

		int rows  = scanner.nextInt();
		int cols  = scanner.nextInt();
		Matrix res = new Matrix(rows,cols);

		for (int r = 0; r < res.rows(); r++) {
			for (int c = 0; c < res.cols(); c++) {
				res.set(r, c, scanner.nextFloat());
			}
		}
		return res;
	}

	private void prepareIndices(int nThreads, Matrix A, Matrix B){
		
		int allIndices = A.cols()* B.cols() * A.rows();
		if(allIndices < nThreads){
			System.out.println("Większa liczba wątków od możliwej liczby zadań.");
			System.exit(-1);
		}
		indices = new int[2*nThreads];
		
		int span = allIndices/nThreads;
		int i;
		for(i = 0 ; i < nThreads -1; i++){
			this.indices[2*i] = i*span;
			this.indices[2*i + 1] = (i+1)*span - 1;
		}
		
		this.indices[2*i] = i*span;
		this.indices[2*i + 1] = allIndices - 1;
	   
	}
	
	private void mul(Matrix A, Matrix B, int nThreads){
		C = new Matrix(A.rows(), B.cols());

		MulThread[] threadsArray = new MulThread[nThreads];
		for(int i = 0; i < nThreads; i++){
			MulThread mulThread = new MulThread(A, B, i, this);
			mulThread.start();
			threadsArray[i] = mulThread;
		}
		
		for(int i = 0; i < nThreads; i++){
			try{
				threadsArray[i].join();
			}catch(InterruptedException e){
				System.out.println("Wątek " + i + " został przerwany.");
			}
		}
	}

	private void print(Matrix m) {
		System.out.println("[");
		for (int r = 0; r < m.rows(); r++) {

			for (int c = 0; c < m.cols(); c++) {
				System.out.print(m.get(r,c));
				System.out.print(" ");
			}

			System.out.println("");
		}
		System.out.println("]");
	}

	private synchronized void updateC(int row, int col, float sum){
		
	float newValue = C.get(row, col) + sum;
		C.set(row, col, newValue);
		
	}
	
	public class Matrix {
		private int ncols;
		private int nrows;
		private float _data[];

		public Matrix(int r, int c) {
			this.ncols = c;
			this.nrows = r;
			_data = new float[c*r];
		}

		public float get(int r, int c) {
			return _data[r*ncols + c];
		}

		public void set(int r, int c, float v) {
			_data[r*ncols +c] = v;
		}

		public int rows() {
			return nrows;
		}

		public int cols() {
			return ncols;
		}
	}
	
	private class MulThread extends Thread {
		
		Matrix A,B;
		int nThread;
		Prir parent;
		
		MulThread(Matrix A, Matrix B, int nThread, Prir parent){
			this.A = A;
			this.B = B;
			this.nThread = nThread;
			this.parent = parent;
		}
		
		@Override
		public void run(){
			
			int bottom = parent.indices[2*nThread];
			int top = parent.indices[2*nThread + 1];
			
			int factor = A.cols()*B.cols();
			
			float sum = 0;
			int oldRowA = -1;
			int oldColumnB = -1;
			
			int rowA = 0;
			int columnA = 0;
			int columnB = 0;	   

			for(int i = bottom; i <= top; i++){
				rowA = i/factor;
				columnB = (i % factor)/A.cols();
				columnA = (i % factor) % A.cols();

				if((oldRowA != -1 && oldColumnB != -1) && 
						(oldRowA != rowA || oldColumnB != columnB)){
					parent.updateC(oldRowA, oldColumnB, sum);
					sum = 0;
				}
				
				sum += A.get(rowA, columnA) * B.get(columnA, columnB);
				oldRowA = rowA;
				oldColumnB = columnB;
			}
			
			parent.updateC(rowA, columnB, sum);
			System.out.println("Wątek " + nThread + " kończy działanie.");
			
		}
	}
}
