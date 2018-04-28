package edu.iastate.cs228.hw5;


import com.sun.org.apache.bcel.internal.classfile.Code;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


/**
 * A class that generates a perfect hash table.
 * CODE TESTED AND WORKS ON LINUX
 * @author  Irfan Farhan Mohamad Rafie
 */
public
class
PerfectHashGenerator
{
  /**
   * The number of rows in the T1 and T2 tables.
   * Enough to fit most English words.
   */
  private
  static
  final
  int
  TABLE_ROWS
    = 8;

  /**
   * The number of columns in the T1 and T2 tables.
   * Enough to fit all English letters.
   */
  private
  static
  final
  int
  TABLE_COLUMNS
    = 64;


  public
  static
  void
  main(String[] args)
  {
    if (null == args || 1 > args.length || 3 < args.length)
    {
      System.err.println("Usage: <word list> [prefix] [seed]");

      System.exit(1);
      return;
    }


    String prefix = "";
    Random rng;
    Random seeder = new Random(25);
    rng = new Random(seeder.nextInt());

    if(args.length >= 2) {
      prefix = args[1];
    }

    if(args.length == 3) {
      rng = new Random(Integer.parseInt(args[2]));
    }

    PerfectHashGenerator gen = new PerfectHashGenerator();
    try
    {
      gen.generate(args[0], prefix + "CHM92Hash", rng);
    }
    catch (IOException e)
    {
      System.err.println(e);

      System.exit(2);
      return;
    }
  }


  /**
   * Generates the perfect hash table for the words in the indicated file, and
   * writes the generated code to the appropriate file
   * ({@code outputClassName + ".java"}).
   *
   * @param wordFileName
   *   the name of the word file
   * @param outputClassName
   *   the name of the output class
   * @param rng
   *   the random number generator for the generated hash table
   *
   * @throws IOException
   *   if the input file cannot be read or the output file cannot be written
   * @throws IllegalArgumentException
   *   if the given output class name is not a valid Java identifier
   */
  public
  void
  generate(String wordFileName, String outputClassName, Random rng)
          throws IOException, IllegalArgumentException
  {
      if(false){
        throw new IllegalArgumentException("Generate input argument error");
      }


      List<String> wordList = readWordFile(wordFileName);
      String outputName = outputClassName + ".java";

      int[][] T1 = new int[TABLE_ROWS][TABLE_COLUMNS], T2 = new int[TABLE_ROWS][TABLE_COLUMNS];

      for(int i = 0; i < TABLE_ROWS; i++){
        for(int y = 0; y < TABLE_COLUMNS; y++){
          T1[i][y] = Math.abs(rng.nextInt(2*wordList.size() + 1) % 2*wordList.size()+1);
          T2[i][y] = Math.abs(rng.nextInt(2*wordList.size() + 1) % 2*wordList.size()+1);
        }
      }


      int[] gArray = new int[2*wordList.size()+1];
      gArray[0] = 0;


      File file = new File("src//edu//iastate//cs228//hw5//" + outputName);

      Graph temp = mapping(T1, T2,2*wordList.size()+1, rng, wordList);
      gArray = temp.fillGArray(wordList.size());
      String test = temp.toString();
    //  System.out.print(test);
      CodeGenerator codeGen = new CodeGenerator(T1, T2, gArray, 2*wordList.size()+1, wordList);
      OutputStream out = new FileOutputStream("src//edu//iastate//cs228//hw5//" + outputName);
      codeGen.generate(out, outputClassName);

  }

  /**
   * Generates the perfect hash table for the given words, and writes the
   * generated code to the given stream.
   *
   * @param words
   *   the list of words for which to generate a perfect hash table
   * @param output
   *   the stream to which to write the generated code
   * @param outputClassName
   *   the name of the output class
   * @param rng
   *   the random number generator for the generated hash table
   *
   * @throws IllegalArgumentException
   *   if the given output class name is not a valid Java identifier
   */
  public
  void
  generate(List<String> words, OutputStream output, String outputClassName,
           Random rng)
    throws IllegalArgumentException
  {
    if(false){
      throw new IllegalArgumentException("Generate input argument error");
    }


    List<String> wordList = words;
    String outputName = outputClassName + ".java";

    int[][] T1 = new int[TABLE_ROWS][TABLE_COLUMNS], T2 = new int[TABLE_ROWS][TABLE_COLUMNS];

    for(int i = 0; i < TABLE_ROWS; i++){
      for(int y = 0; y < TABLE_COLUMNS; y++){
        T1[i][y] = Math.abs(rng.nextInt(2*wordList.size() + 1) % 2*wordList.size()+1);
        T2[i][y] = Math.abs(rng.nextInt(2*wordList.size() + 1) % 2*wordList.size()+1);
      }
    }


    int[] gArray = new int[2*wordList.size()+1];
    gArray[0] = 0;


    //File file = new File("src//edu//iastate//cs228//hw5//" + outputName);

    Graph temp = mapping(T1, T2,2*wordList.size()+1, rng, wordList);
    gArray = temp.fillGArray(wordList.size());
    String test = temp.toString();
  //  System.out.print(test);
    CodeGenerator codeGen = new CodeGenerator(T1, T2, gArray, 2*wordList.size()+1, wordList);
    codeGen.generate(output, outputClassName);

  }

  /**
   * Performs the mapping step for generating the perfect hash table.
   *
   * Precondition: the list of keys contains no duplicate values.
   *
   * @param table1
   *   the T1 table
   * @param table2
   *   the T2 table
   * @param modulus
   *   the modulus
   * @param rng
   *   the random number generator to use
   * @param words
   *   the list of keys for the hash table
   * @return
   *   the generated graph
   *
   * @throws IllegalArgumentException
   *   if the modulus is not positive
   */
  private
  Graph
  mapping(int[][] table1, int[][] table2, int modulus, Random rng, List<String> words)
    throws IllegalArgumentException
  {
    Graph toRet;
    //Visualizer vis = new Visualizer();  //REMOVE AFTER USE

    do
    {
      toRet = new Graph(modulus);
     // vis.useGraph(toRet);    //REMOVE AFTER USE
      for (int r = 0; r < TABLE_ROWS; ++r)
      {
        for (int c = 0; c < TABLE_COLUMNS; ++c)
        {
          table1[r][c] = rng.nextInt(modulus);
          table2[r][c] = rng.nextInt(modulus);
        }
      }

      for (int i = 0; i < words.size(); ++i)
      {
        String w = words.get(i);
        int f1 = 0, f2 = 0;

        for (int j = 0; j < w.length(); ++j)
        {
          f1 += table1[j % TABLE_ROWS][w.charAt(j) % TABLE_COLUMNS];
          f2 += table2[j % TABLE_ROWS][w.charAt(j) % TABLE_COLUMNS];
        }

        f1 %= modulus;
        f2 %= modulus;

        toRet.addEdge(f1, f2, i, w);
      }
    }
    while (toRet.hasCycle());


    return toRet;
  }

  /**
   * Reads the indicated file, making a list containing the lines within it.
   *
   * @param fileName
   *   the file to read
   * @return
   *   a list containing the lines of the indicated file
   *
   * @throws FileNotFoundException
   *   if the indicated file cannot be read
   */
  private
  List<String>
  readWordFile(String fileName)
    throws FileNotFoundException
  {

    Scanner input = new Scanner(new FileReader(fileName));
    ArrayList<String> inputList = new ArrayList<>();
    while(input.hasNext()){
      inputList.add(input.next());
    }

    return inputList;
  }
}
