package arp;

public class CircularArrayQueue<T> {
	// Constants
	private static final int DEFAULT_CAPACITY = 5 ;
	// Instance Variables
	private int _capacity ;
	private int _front ;
	private int _rear ;
	private T[] _elements ;
	
	//생성자
	@SuppressWarnings("unchecked")
	public CircularArrayQueue (int givenCapacity){
		this._capacity = givenCapacity;
		this._front = 0;
		this._rear = 0;
		this._elements = (T[ ]) new Object[this._capacity] ;
		
	}
	@SuppressWarnings({ "unchecked", "static-access" })
	public CircularArrayQueue (){
		this._capacity = this.DEFAULT_CAPACITY;
		this._front = 0;
		this._rear = 0;
		this._elements = (T[ ]) new Object[this._capacity] ;
	}
	
	//공개함수
	public int capacity(){
		//this._capacity 값을 반환
		return this._capacity;
	}
	public boolean isEmpty (){
		//this._front 와 this._rear 가 같은지 확인하여 같으면 true 를 반환
		return (this._front == this._rear);
	}
	public boolean isFull (){
		//다음 삽입될 위치가 _front 와 같은지 확인하여 같으면 true 를 반환
		return ( ( ( this._rear + 1 ) % this._capacity ) == this._front );
	}
	public int size (){
		if ( this._front <= this._rear ){
			return ( this._rear - this._front );
		}
		else{
			return ( this._rear + this._capacity - this._front );
		}
	}
	public T frontElement (){
		//비어 있지 않은 경우 가장 앞에 있는 값을 반환
		if ( this.isEmpty() ){
			return null;
		}
		else{
			return ( this._elements[this._front + 1] );
		}
	}
	public boolean enQueue (T anElement){
		if ( this.isFull() ){
			return false;
		}
		else{
			 this._rear = ( this._rear + 1 ) % this._capacity;
			 this._elements[this._rear] = anElement;
			 return true;
		}
	}
	public T deQueue (){
		if( this.isEmpty() ){
			return null;
		}
		else{
			this._front = ( this._front + 1) % this._capacity;
			T anElement = this._elements[this._front];
			this._elements[this._front] = null;
			return anElement;
		}
	}
	public void clear (){
		this._front = 0;
		this._rear  = 0;
		for ( int i = 0 ; i < this.size() ; i++ ) {
			this._elements[(this._front) % this.capacity()] = null ;
		}

	}
	public T elementAt (int aPosition){
		return this._elements[ (this._front + 1 + aPosition) % this._capacity ];
	}
}
