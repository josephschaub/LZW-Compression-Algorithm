/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
	private static final int maxL = 65536;		//max value for L, 2^16, 16 being the max for W

    public static void compress(String type) 
	{ 
		double oldRatio = 0;
		double newRatio = 0;
		double uncompressedDataSize = 0;
		double compressedDataSize = 0;
		
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
		
		BinaryStdOut.write(type);		//write the compression type to the file for expansion
		
		
        while (input.length() > 0) 
		{
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
			
			uncompressedDataSize = uncompressedDataSize + (t * 8);		// adding t*8 because t is the uncompressed data we are representing * 8 bits
			compressedDataSize = compressedDataSize + W;				//adding the current size in bits of the codeword because that is what is written to the compressed file
			
            if (t < input.length() && code < L)    // Add s to symbol table.
			{
                st.put(input.substring(0, t + 1), code++);
				oldRatio = uncompressedDataSize / compressedDataSize;
			}
			else if(t < input.length() && W < 16 && code >= L)				//if we've added L codewords to the codebook, increase the width of the codeword and increase L by 2
			{
				W = W + 1;
				L = L * 2;
				st.put(input.substring(0, t + 1), code++);
			}
			else if(t < input.length() && W == 16 && code == L)
			{
				if(type.equals("r"))			//reset mode
				{
					st = new TST<Integer>();			//reinitialize the codebook
					for (int i = 0; i < R; i++)
						st.put("" + (char) i, i);
					L = 512;							//reset L and W back to their original values
					W = 9;
					code = R+1;  // R is codeword for EOF
					st.put(input.substring(0, t + 1), code++);
				}
				else if(type.equals("m"))		//monitor mode
				{
					newRatio = uncompressedDataSize / compressedDataSize;		//the new compression ratio is the size of the uncompressed data over the size of the compressed data
					
					if((oldRatio / newRatio) > 1.1)				//if the compression ratio is getting too bad, reset the codebook
					{
						st = new TST<Integer>();			//reinitialize the codebook
						for (int i = 0; i < R; i++)
							st.put("" + (char) i, i);
						L = 512;							//reset L and W back to their original values
						W = 9;
						code = R+1;  // R is codeword for EOF
						st.put(input.substring(0, t + 1), code++);
					}
					
				}
				else if(type.equals("n"))		//do nothing mode
				{
					//literally just do nothing
				}
			}
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() 
	{
        String[] st = new String[maxL];			//set the codebook to the maximum number of codewords
        int i; // next available codeword value
		char mode;
		double oldRatio = 0;
		double newRatio = 0;
		double uncompressedDataSize = 0;
		double compressedDataSize = 0;

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";          		// (unused) lookahead for EOF
		mode = BinaryStdIn.readChar();
		

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) 
			return;           // expanded message is empty string
        String val = st[codeword];

        while(true) 
		{
            BinaryStdOut.write(val);
			
			uncompressedDataSize = uncompressedDataSize + (val.length() * 8);		// adding val*8 because val is the ucompressed data we are representing * 8 bits
			compressedDataSize = compressedDataSize + W;				//adding the current size of the codeword in bits because that is what is written to the uncompressed file
			
			
			
			if(W < 16 && i >= L)				//if we've added L codewords to the codebook, increase the width of the codeword and increase L by 2
			{
				W = W + 1;
				L = L * 2;
			}
			else if(W == 16 && i == L)
			{
				if(mode == 'r')						//reset mode
				{
					st = new String[maxL];			//reinitialize the codebook
					for (i = 0; i < R; i++)
						st[i] = "" + (char) i;
					L = 512;							//reset L and W back to their original values
					W = 9;
					i = R + 1;				//R is codeword for EOF
				}
				else if(mode == 'm')				//monitor mode
				{
					newRatio = uncompressedDataSize / compressedDataSize;			//the new compression ratio is the size of the uncompressed data over the size of the compressed data
					
					if((oldRatio / newRatio) > 1.1)			//if the compression ratio is getting too bad, reset the codebook
					{
						st = new String[maxL];
						for (i = 0; i < R; i++)
							st[i] = "" + (char) i;
						L = 512;							//reset L and W back to their original values
						W = 9;
						i = R + 1;		//r is codeword for EOF
					}

				}
				else if(mode == 'n')				//do nothing mode
				{
					//again, just do nothing lol
				}
				
			}
			
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) 
				break;
            String s = st[codeword];
            if (i == codeword) 
				s = val + val.charAt(0);   // special case hack
            if (i < L) 
			{
				st[i++] = val + s.charAt(0);
				oldRatio = uncompressedDataSize / compressedDataSize;			//set the oldRatio for comparison in the next loop
			}
            val = s;
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if      (args[0].equals("-")) compress(args[1]);				//"args[1]" is added to pass the compression mode into the compress function
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
