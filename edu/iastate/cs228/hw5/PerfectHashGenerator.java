package edu.iastate.edu.cs228.hw5;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;


/**
 * A class that generates a perfect hash table.
 *
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


    // TODO remove this and process args properly
    rng = null;


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
    throws IOException,
           IllegalArgumentException
  {
    // TODO
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
    // TODO
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

    do
    {
      toRet = new Graph(modulus);

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
    // TODO
    return null;
  }
}
