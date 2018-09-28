package bestan.test.thread;

import java.io.FileInputStream;

/**
 * @author yeyouhuan
 *
 */
public class TestFile {
	public static void main(String[] args) {
		try {
			var rf = new FileInputStream("test1.txt");
			//RandomAccessFile rf = new RandomAccessFile("test1.txt", "r");
			while (rf.getFD().valid()) {
				System.out.println("TestFile:" + rf.read());
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
