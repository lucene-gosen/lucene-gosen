package net.java.sen.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.java.sen.util.CSVParser;

/**
 * Preprocess an unpacked Naist-JDic for MeCab dictionary into the CSV form used
 * for compilation.<br/>
 * 
 * 
 * 
 */
public class MeCabDicPreprocessor {
  
  /**
   * Input dictionary CSV filename
   */
  private static final String INPUT_DICTIONARY_CSV_FILENAME = "naist-jdic.csv";
  
  /**
   * Input connection cost definition file.
   */
  private static final String INPUT_MATRIX_DEF_FILENAME = "matrix.def";
  
  /**
   *  left-id.def<br>
   *  left context id
   */
  private static final String INPUT_LEFT_ID_DEF_FILENAME = "left-id.def";
  /**
   *  right-id.def<br>
   *  right context id
   */
  private static final String INPUT_RIGHT_ID_DEF_FILENAME = "right-id.def";
  
  // TODO rewrite.def .
  private static final String INPUT_REWRITE_DEF_FILENAME = "rewrite.def";
  
  // TODO no-use files.
  // feature.def
  // pos-id.def
  
  /**
   * Input file charset.
   */
  private String charset;
  
  /**
   * Output dictinary filename
   */
  private static final String OUTPUT_DICTIONARY_CSV_FILENAME = "dictionary.csv";
  
  /**
   * Output connection filename
   * 
   */
  private static final String OUTPUT_CONNECTION_CSV_FILANAME = "connection.csv";
  
  /**
   * output charset
   */
  private static final String OUTPUT_CHARSET = "UTF-8";
    
  /**
   * mapping right-id.def
   */
  private Map<Integer,String> rightIdMap;
  
  /**
   * mapping left-id.def
   */
  private Map<Integer,String> leftIdMap;
  
  /**
   * directory source dictionary
   */
  private String inputDirectory;
  
  /**
   * load mapping from (left|right)-id.def
   * 
   * @param fileName def filename(right-id.def | left-id.def)
   * @param charset charset name
   * @return idMap
   */
  private Map<Integer,String> loadIdMap(String fileName, String charset)
      throws IOException {
    Map<Integer,String> map = new HashMap<Integer,String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset));
    String line = null;
    String[] columns = null;
    Integer id = null;
    
    while ((line = reader.readLine()) != null) {
      if (line != null && !line.isEmpty()) {
        columns = line.split("\\s",2);
        id = Integer.parseInt(columns[0]);
        //TODO test implements
        map.put(id, rewriteRule( columns[1]));
      }
    }
    reader.close();
    
    return map;
  }
  
  /**
   * TODO naist-jdic for mecab include euc-jp
   * @param src
   * @return 
   */
  private String convertUnicode(String src){
    
    return src;
  }
  
  /**
   * FIXME maybe rewrite.def logic implements
   * @param pattern 
   * @return 
   */
  private String rewriteRule(String pattern){
    if(pattern.endsWith("BOS/EOS") && pattern.startsWith("BOS/EOS")){
      return pattern.replace("BOS/EOS", "*");
    }else{
      return pattern;
    }
  }
  
  // current MeCab ignore first left pos.
  // FIXME 構造の解析（特にmatrixの違いの検討）->*****として一番左は何でもOKとする。
  private static final String FIRST_CONNECTION_PATTERN ="*,*,*,*,*,*,*";
  
  
  private String getInputFile(String fileName){
    return this.inputDirectory+File.separator+fileName;
  }
  
  /**
   * Builds a connection CSV file from naist-jdic for mecab
   * @param outputFilename The filename for the connection CSV file
   * 
   */
  public void buildConnectionCSV(String outputFilename)
      throws UnsupportedEncodingException, FileNotFoundException, IOException {
    // load right-id.def
    rightIdMap = loadIdMap(getInputFile(INPUT_RIGHT_ID_DEF_FILENAME), charset);
    // load left-id.def
    leftIdMap = loadIdMap(getInputFile(INPUT_LEFT_ID_DEF_FILENAME), charset);
    
    // load matrix.def
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getInputFile(INPUT_MATRIX_DEF_FILENAME)), charset));
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), OUTPUT_CHARSET));
    
    String line;
    String[] columns;
    Integer rightId;
    Integer leftId;
    StringBuilder builder = new StringBuilder();
    boolean header = true;
    while ((line = reader.readLine()) != null) {
      if(header){
        //first line is id size header
        header=false;
        continue;
      }
      columns = line.split("\\s",3);
      leftId = Integer.parseInt(columns[0]);
      rightId = Integer.parseInt(columns[1]);
      
      builder.replace(0, builder.length(), "\"").append(FIRST_CONNECTION_PATTERN).append("\",\"");
      builder.append(leftIdMap.get(leftId));
      builder.append("\",\"");
      builder.append(rightIdMap.get(rightId));
      builder.append("\",");
      try{
        builder.append(columns[2]);
      }catch(Exception ex){
        System.err.println(line);
        throw new IOException(ex);
      }
      builder.append("\n");
      writer.write(builder.toString());
    }
    
    writer.close();
    
  }
  
  /**
   * Builds a dictionary CSV file from an unpacked naist-jdic for mecab
   * 
   * @param outputFilename The filename to use for the dictionary CSV file
   */
  public void buildDictinaryCSV(String outputFilename) throws UnsupportedEncodingException, FileNotFoundException, IOException{
    
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), OUTPUT_CHARSET));
    
    //load source dic file
    CSVParser csvParser = new CSVParser(new FileInputStream(getInputFile(INPUT_DICTIONARY_CSV_FILENAME)), charset);
    
    String[] columns;
    while ((columns = csvParser.nextTokens()) != null) {
      
      String csvLine = String.format("\"%1$s\",%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,\"%9$s\",\"%10$s\",\"%11$s\"\n",
          columns[0], columns[3], columns[4], columns[5], columns[6], columns[7], columns[8], columns[9], columns[10], columns[11], columns[12]);
      writer.write(csvLine);
    }
    
    writer.close();

    

  }
  
  /**
   * Preprocesses the dictionary
   * @param outputDirectory The directory to write the preprocessed dictionary to
   * @throws IOException
   */
  public void build(String outputDirectory) throws IOException{
    buildConnectionCSV(outputDirectory + File.separator + OUTPUT_CONNECTION_CSV_FILANAME);
    buildDictinaryCSV(outputDirectory + File.separator + OUTPUT_DICTIONARY_CSV_FILENAME);
  }
  
  

  /**
   * Creates a new preprocessor for the unpacked dictionary in the given dictionary(naist-jdic for mecab)
   * 
   * @param charset The charset used to read the dictionary
   * @param inputDirectory The directory of the unpacked dictionary
   */
  public MeCabDicPreprocessor(String charset, String inputDirectory) {
    this.charset = charset;
    this.inputDirectory = inputDirectory;
  }
  
  
  
}
