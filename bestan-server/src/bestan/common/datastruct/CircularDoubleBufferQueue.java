/**************************************
 * DoubleBufferQueue
 **************************************/

package bestan.common.datastruct;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// implements BlockingQueue<E> and Serializable old code
// extends AbstractQueue<E> old code
public class CircularDoubleBufferQueue<E>  implements Serializable {
	private static final long serialVersionUID = 1L;  
//	private static Logger logger = Log4J.getLogger(CircularDoubleBufferQueue.class);
	
	/** The queued items  */  
    private final E[] itemsA;  
    private final E[] itemsB;  
      
    private ReentrantLock readLock, writeLock;  
    @SuppressWarnings("unused")
	private Condition notEmpty;  
    private Condition notFull;  
    private Condition awake;  
      
    private E[] writeArray, readArray;  
    private volatile int writeCount, readCount;  
    @SuppressWarnings("unused")
	private int writeArrayHP, writeArrayTP, readArrayHP, readArrayTP;  
      
      
    @SuppressWarnings("unchecked")
	public CircularDoubleBufferQueue(int capacity) {  
        if(capacity <= 0)  
        {  
            throw new IllegalArgumentException("Queue initial capacity can't less than 0!");  
        }  
          
        itemsA = (E[])new Object[capacity];  
        itemsB = (E[])new Object[capacity];  
  
        readLock = new ReentrantLock();  
        writeLock = new ReentrantLock();  
          
        notEmpty = readLock.newCondition();  
        notFull = writeLock.newCondition();  
        awake = writeLock.newCondition();  
          
        readArray = itemsA;  
        writeArray = itemsB;  
    }  
      
    private void insert(E e) {  
        writeArray[writeArrayTP] = e;  
        ++writeArrayTP;  
        ++writeCount;  
    }  
      
    private E extract() {  
        E e = readArray[readArrayHP];  
        readArray[readArrayHP] = null;  
        ++readArrayHP;  
        --readCount;  
        return e;  
    }  
  
      
    /** 
     *switch condition:  
     *read queue is empty && write queue is not empty 
     *  
     *Notice:This function can only be invoked after readLock is  
         * grabbed,or may cause dead lock 
     * @param timeout 
     * @param isInfinite: whether need to wait forever until some other 
     * thread awake it 
     * @return 
     * @throws InterruptedException 
     */  
    private long queueSwitch(long timeout, boolean isInfinite) throws InterruptedException  
    {  
        writeLock.lock();  
        try  
        {  
            if (writeCount <= 0)  
            {  
                System.out.println("Write Count:" + writeCount + ", Write Queue is empty, do not switch!");  
                try  
                {  
                	System.out.println("Queue is empty, need wait....");  
                    if(isInfinite && timeout<=0)  
                    {  
                        awake.await();  
                        return -1;  
                    }  
                    else  
                    {  
                        return awake.awaitNanos(timeout);  
                    }  
                }  
                catch (InterruptedException ie)  
                {  
                    awake.signal();  
                    throw ie;  
                }  
            }  
            else  
            {  
                E[] tmpArray = readArray;  
                readArray = writeArray;  
                writeArray = tmpArray;  
  
                readCount = writeCount;  
                readArrayHP = 0;  
                readArrayTP = writeArrayTP;  
  
                writeCount = 0;  
                writeArrayHP = readArrayHP;  
                writeArrayTP = 0;  
                  
                notFull.signal();  
//                System.out.println("Queue switch successfully! readcount:" + readCount);  
                return -1;  
            }  
        }  
        finally  
        {  
            writeLock.unlock();  
        }  
    }  
  
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException  
    {  
        if(e == null)  
        {  
            throw new NullPointerException();  
        }  
          
        long nanoTime = unit.toNanos(timeout);  
        writeLock.lockInterruptibly();  
        try  
        {  
            for (;;)  
            {  
                if(writeCount < writeArray.length)  
                {  
                    insert(e);  
                    if (writeCount == 1)  
                    {  
                        awake.signal();  
                    }  
                    return true;  
                }  
                  
                //Time out  
                if(nanoTime<=0)  
                {  
                	System.out.println("offer wait time out!");  
                    return false;  
                }  
                //keep waiting  
                try  
                {  
                	System.out.println("Queue is full, need wait....");  
                    nanoTime = notFull.awaitNanos(nanoTime);  
                }  
                catch(InterruptedException ie)  
                {  
                	System.out.println("offer catch not full signal...."); 
                    notFull.signal();  
                    throw ie;  
                }  
            }  
        }  
        finally  
        {  
            writeLock.unlock();  
        }  
    }  
  
    public E poll(long timeout, TimeUnit unit) throws InterruptedException  
    {  
        long nanoTime = unit.toNanos(timeout);  
//        readLock.lockInterruptibly();  
          
        try  
        {  
            for(;;)  
            {  
                if(readCount>0)  
                {  
                    return extract();  
                }  
                  
                if(nanoTime<=0)  
                {  
                	System.out.println("poll time out!");  
                    return null;  
                }  
                nanoTime = queueSwitch(nanoTime, false);  
            }  
        }  
        finally  
        {  
//            readLock.unlock();  
        }  
    }  
    
    public int getReadLength() {
    	return this.readCount;
    }
    
    // this is thread safe 
    // cause this.writeCount is voliate
    public int getWriteLength() {
    	return this.writeCount;
    }
}
