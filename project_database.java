/* --
COMP4321 Project Phase 1
Group 11 Member
Xu, Feiting(fxuaf)  20329359
Lam Hon Wa(hwlamad) 20348745
Li, Junze(jlicx)    20413186

*/

//import StopStem.*;
import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

import java.net.URL;
import java.rmi.server.RMISocketFactory;
import java.net.HttpURLConnection; //for get_info
import java.util.Vector;
import java.lang.Math;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

import java.util.*;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Date; // for date in get_info


//import jsoup & file i/o
import org.jsoup.Jsoup;  
import org.jsoup.nodes.Document; 
import org.jsoup.nodes.Element;
import java.io.*;
import java.io.IOException;  
import java.io.FileWriter; 



public class project_database{
	//Data Member
	private String path_key=null;
	private static Options options;

	// see the document for detailed description of the DBs
	private static RocksDB url;
	private static RocksDB word;
	private static RocksDB forward;
	private static RocksDB inv;
	private static RocksDB title_inv;
    private static RocksDB parent_child_relation;
    private static RocksDB child_parent;
	private static RocksDB page_info;
	private static RocksDB idf;
	private static RocksDB inv_url;
	private static RocksDB inv_word;
    private static RocksDB history;
    private static RocksDB term_weight;
    private static RocksDB list_keyword;

	private static StopStem stopStem;

	//end of Data Member

	//Constructor
 	project_database() throws RocksDBException{
 		//stopStem = new StopStem();

 		options = new Options();
        options.setCreateIfMissing(true);

        try
        {
            // a static method that loads the RocksDB C++ library.
            RocksDB.loadLibrary();

            String path1 = "db/url";
            String path2 = "db/word";
            String path3 = "db/forward";
            String path4 = "db/inv";
            String path5 = "db/title_inv";
            String path6 = "db/parent_child_relation";
            String path7 = "db/page_info";
            String path8 = "db/idf";
            String path9 = "db/history";
            String path10 = "db/inv_url";
            String path11 = "db/inv_word";
            String path12 = "db/child_parent";
            String path13 = "db/term_weight";
            String path14 = "db/list_keyword";//in alphabet order

            // create/open database
            url = RocksDB.open(options,path1); 
            byte[] max_page = url.get("max_id".getBytes());
            if(max_page==null) url.put("max_id".getBytes(),(new String(String.valueOf(0)).getBytes()));

            word = RocksDB.open(options,path2);
            byte[] max_word = word.get("max_id".getBytes());
            if(max_word==null) word.put("max_id".getBytes(),(new String(String.valueOf(0)).getBytes()));

            forward = RocksDB.open(options,path3);
            inv = RocksDB.open(options,path4);
            title_inv = RocksDB.open(options,path5);
            parent_child_relation = RocksDB.open(options,path6);
            page_info = RocksDB.open(options,path7);
            idf = RocksDB.open(options,path8);
            history = RocksDB.open(options,path9);
            inv_url = RocksDB.open(options,path10);
            inv_word = RocksDB.open(options,path11);
            child_parent = RocksDB.open(options,path12);
            term_weight = RocksDB.open(options,path13);
            list_keyword = RocksDB.open(options,path14);
        }
        catch(RocksDBException e)
        {
            System.err.println(e.toString());
        }

 	}
 
    public static int get_urlID(String Url) throws RocksDBException
    {
        byte[] content = url.get(Url.getBytes());
        if(content==null){
            int num_page = Integer.parseInt(new String(url.get("max_id".getBytes())));
            num_page+=1;
            url.put("max_id".getBytes(),new String(String.valueOf(num_page)).getBytes());
            url.put(Url.getBytes(),new String(String.valueOf(num_page)).getBytes());
            inv_url.put(new String(String.valueOf(num_page)).getBytes(),Url.getBytes());
            return num_page;
        }
        else{
        	int page_id = Integer.parseInt(new String(content));
        	return page_id;
        }
       
    }
    
    public static int get_wordID(String Term) throws RocksDBException
    {
       byte[] content = word.get(Term.getBytes());
        if(content==null){
            int num_word = Integer.parseInt(new String(word.get("max_id".getBytes())));
            num_word+=1;
            word.put("max_id".getBytes(),new String(String.valueOf(num_word)).getBytes());
            word.put(Term.getBytes(),new String(String.valueOf(num_word)).getBytes());
            inv_word.put(new String(String.valueOf(num_word)).getBytes(),(Term+" "+ "0").getBytes()); //set it to 0 first, and will be count after crawl
            return num_word;
        }
        else{
        	int term_id = Integer.parseInt(new String(content));
        	return term_id;
        }
       
    }

    public static void calculation(int page_no) throws RocksDBException
    {   
    for (int i=1; i< page_no+1; i++){                                                    //calculate the df first
        //System.out.println("page"+ i + "start df calculation: ");
        String current_page = String.valueOf(i);  
        byte[] content = forward.get((current_page).getBytes());                         //get content from forward db
        if (content == null){continue;}
        else {
            String[] term_lists = (new String(content)).split(",");                      //split term_ids
            for (int j=0; j< term_lists.length; j++){                                    //for each term_id
                String[] term_id_lists = term_lists[j].split(" ");                       //split term_id and tf
                byte[] word_info = inv_word.get((term_id_lists[0]).getBytes());          //search content of term_id
                String[] lists = (new String (word_info)).split(" ");                    //split word and df
                int df =  Integer.parseInt(lists[1]);                           
                df = df +1;                                                              //increase df
                String word = lists[0];
                String word_content = new String(word + " " + df);                       //combine word and df
                inv_word.put(term_id_lists[0].getBytes(), word_content.getBytes());      //put key, content in inv_word
                //System.out.println("term_id" + term_id_lists[0]+ " df" + new String(inv_word.get(term_id_lists[0].getBytes())));
            }
        }
    }

    String item_no = new String (word.get("max_id".getBytes()));
    int max_term = Integer.parseInt(item_no);
    for (int i=1; i<= max_term; i++){                                                    //calculate the idf
        String current_term = String.valueOf(i);  
        String term_content = new String (inv_word.get(current_term.getBytes()));
        String[] lists = (new String (term_content).split(" "));                        //split word and df
        double df =  Integer.parseInt(lists[1]);
        if(df>5){
            if(lists[0]!=""){
            char first_letter_char = lists[0].charAt(0);
            if(first_letter_char-'a'>=0 && first_letter_char-'a'<=25){
            String first_letter = Character.toString(lists[0].charAt(0));
            //System.out.println("putting keyword:"+lists[0]+" "+first_letter);
            byte[] list_content = list_keyword.get(first_letter.getBytes());
            String new_content="";
            if(list_content==null){
                new_content = lists[0];
            }
            else{
                String pre = new String(list_content);
                new_content = pre + " "+lists[0];
            }
            //System.out.println("new content:"+first_letter+" "+new_content);
            list_keyword.put(first_letter.getBytes(),new_content.getBytes());
            }
        }
        }   
        double N =   page_no;                             
        double idf_no = (Math.log(N/df)/Math.log(2));                                   //calculate the idf
        idf_no = Math.floor(idf_no*1000)/1000;
        idf.put (current_term.getBytes(), (String.valueOf(idf_no)).getBytes());
        //System.out.println("idf: "+ new String (idf.get(current_term.getBytes())));
    }
        //for debuging!!
        String[] str_array = new String[26];
        for(char i ='a';i<='z';i++){
            String ss = Character.toString(i);
            //System.out.println(ss);
            byte[] content_lk = list_keyword.get(ss.getBytes());
            if(content_lk!=null){
                str_array[i-'a'] = new String(content_lk);
                System.out.println(i+" "+str_array[i-'a']);

            }
        }
        //for debuging!!

    for (int i=1; i< page_no+1; i++){
        //System.out.println("page"+ i + "start: ");
        String current_page = String.valueOf(i);  
        byte[] content = forward.get((current_page).getBytes());                         //get content from forward db
        if (content == null){ System.out.println("error in "+ current_page);
            continue;}
        else {
            String[] term_lists = (new String(content)).split(",");                      //split term_ids                                                    
            String[] max_term_id = term_lists[0].split(" ");                             //get the max number of term ids
            int max_tf = Integer.parseInt(max_term_id[1]);
            double D = 0;

            for (int j=0; j< term_lists.length-1; j++){                                    //for each term_id
                String[] term_id_lists = term_lists[j].split(" ");                       //split term_id and tf
                String idf_content = new String (idf.get(term_id_lists[0].getBytes()));
                double idf_no = Double.parseDouble(idf_content);//get the idf
                double term_freq = Integer.parseInt(term_id_lists[1]);
                double term_weight_no = (term_freq * idf_no /max_tf);                    //calculate the termweight
                double term_weight_input = Math.floor(term_weight_no*1000)/1000;
                byte[] term_weight_content = term_weight.get(current_page.getBytes());
                if (term_weight_content == null){                                        //put term_weight in db
                    term_weight.put(current_page.getBytes(), (term_id_lists[0] +" "+ term_weight_input + ",").getBytes());
                }
                else {
                    term_weight.put(current_page.getBytes(), ( (new String (term_weight_content)) + term_id_lists[0] +" "+ term_weight_input + ",").getBytes());}
                //System.out.println("term_weight " + (new String (term_weight.get(current_page.getBytes()))));
                D = D+ term_weight_input*term_weight_input;                                    //add all term_weight^2 in D
                //System.out.println("term_id: "+ term_id_lists[0]+ "N:" + N + " df: "+ df + " idf: " + idf_no + " Term_weight " + term_weight_input + ",");
            }
        
        double final_D =  Math.floor(Math.pow(D,0.5)*1000)/1000;                                               //sqrt root the D
        String pageInfo = new String (page_info.get(current_page.getBytes()));
        byte[] p_content = (pageInfo + ";" +String.valueOf(final_D)).getBytes();
        page_info.put(current_page.getBytes(), p_content);
        //System.out.println("inv_url " + (new String (url_content)));
            }
        }
     }   



    public static void add_parent_child(int page_id, String child_id) throws RocksDBException
    {
        byte[] key = (new String(String.valueOf(page_id))).getBytes();
        byte[] value = child_id.getBytes();
        parent_child_relation.put(key, value);
        //format: parent_id -> c1_id, c2_id,
    }

    public static void addEntry_inv(int tid, int pid, int pos) throws RocksDBException
    {
        
        byte[] content = inv.get((new String(String.valueOf(tid))).getBytes());
        if (content == null) {
            content = (new String(String.valueOf(pid))+" "+new String(String.valueOf(pos))+",").getBytes();
        }
        else{
            content = (new String(content)+new String(String.valueOf(pid))+" "+new String(String.valueOf(pos))+",").getBytes();
        }
        inv.put((new String(String.valueOf(tid))).getBytes(), content);
    }

    public static void addEntry_title_inv(int tid, int pid, int pos) throws RocksDBException
    {
        
        byte[] content = title_inv.get((new String(String.valueOf(tid))).getBytes());
        if (content == null) {
            content = (new String(String.valueOf(pid))+" "+new String(String.valueOf(pos))+",").getBytes();
        }
        else{
            content = (new String(content)+new String(String.valueOf(pid))+" "+new String(String.valueOf(pos))+",").getBytes();
        }
        title_inv.put((new String(String.valueOf(tid))).getBytes(), content);
    }

    public static void addEntry_child_parent(String child_id, int parent_id) throws RocksDBException
    {
        
        byte[] content = child_parent.get(child_id.getBytes());
        if (content == null) {
            content = (String.valueOf(parent_id)).getBytes();
        }
        else{
            content = (new String(content) +" "+ String.valueOf(parent_id)).getBytes();
        }
        child_parent.put(child_id.getBytes(), content);
        String content_string = new String (content);
        //System.out.println("child_id: " + child_id + " parent_id:" + content_string);
    } 
    

    public static void addEntry_forward(int pid, String ff) throws RocksDBException
    {
        forward.put((new String(String.valueOf(pid))).getBytes(), ff.getBytes());
    }


    public static URL[] extractLinks(String link) throws ParserException

	{
		// extract links in url and return them
	    //Vector<String> v_link = new Vector<String>();
	    LinkBean lb = new LinkBean();
	    lb.setURL(link);
	    URL[] URL_array = lb.getLinks();
        return URL_array;
    }

    public static void crawl(String URL,int num) throws RocksDBException{
        Queue<String> URL_queue = new LinkedList<>();
        URL_queue.add(URL);
        int process_num=0;
        while(process_num<num && !URL_queue.isEmpty()){
        	//process in the loop
       	String curUrl = URL_queue.remove();
        process_num += 1;
       	int pid = get_urlID(curUrl);
        System.out.println("process "+ process_num+" "+curUrl);

        //Add the title,date,size to database 7
        //get the title by jsoup
        try{try{
            //try{
        Document doc = Jsoup.connect(curUrl).get();  
        String title = doc.title();  

        //get the size and date
        URL url_temp = new URL(curUrl);
        HttpURLConnection conn = (HttpURLConnection)url_temp.openConnection();
        String size = String.valueOf(conn.getContentLength());

        StringBean sb = new StringBean();                   //process the text
        sb.setLinks(false);
        sb.setURL(curUrl);
        String all_content = sb.getStrings();
        System.out.println("all_content:" + all_content);
        if (size.equals("-1")){
            int size_count = all_content.length() ;
            size = "Content length: " + String.valueOf(size_count) + " characters"; 
        } else { size = "Content size: " + size;}

        String date = conn.getHeaderField("Last-Modified");
        if (date == null){
            Date current_date = new Date();
            date = "Last Modified date: not found" + "(Crawled at " + new String (String.valueOf(current_date)+")") ;
        } else { date = date.replace(",","");
                 date = "Last Modified: " + date;
        }

        byte[] key = (new String(String.valueOf(pid))).getBytes();
        String content_url = title + ";" + size + ";" + date;
        byte[] value = content_url.getBytes();
        System.out.println("page info put"+pid+" "+content_url);
        page_info.put(key, value);
        
        
       	//process parent_child
       	URL[] child_URL_array = project_database.extractLinks(curUrl);
        //here I ignored the following condition first
        //If the URL already exists in the index but the last modification date 
        //of the URL is later than that recorded in the index, go ahead to retrieve the URL; 
        //otherwise, ignore the URL
        String child_id="";
        
        if (child_URL_array.length != 0){
            int[] cid_check = new int [child_URL_array.length];  
            cid_check[0]=-1;
            int max_j=0;
            for(int i = 0; i < child_URL_array.length; i++){
                byte[] content = url.get((child_URL_array[i].toString()).getBytes());
                int cid = get_urlID((child_URL_array[i].toString()));
                //System.out.println(cid);
                if(content==null){ //child not being processed
                    URL_queue.add((child_URL_array[i].toString()));
                }
                boolean test = true;
                for (int j=0; j<max_j; j++){           //didn't add child if the child has already been pointed
                    //System.out.println("cid_check: " + cid_check[j] + " " + String.valueOf(cid)) ;
                    if (cid_check[j] == cid){
                        //System.out.println("repeat");
                        test = false;
                        break;}
                }
                if (test == true){
                    cid_check[max_j]= cid;
                    //System.out.println("cid&child: " +String.valueOf(cid) + " " + test);
                    addEntry_child_parent(String.valueOf(cid), pid);
                    child_id += (new String(String.valueOf(cid))+",");
                    max_j ++;
                }
            }
            add_parent_child(pid,child_id);
        }
        //add the URL and child_URL to database 6

        //}catch (ParserException e){ System.err.println(e.toString());}
		//finish parent_child

		//process text
		/*
		StringBean sb = new StringBean();
        sb.setLinks(false);
        sb.setURL(curUrl);
        String all_content = sb.getStrings();*/
        //String[] title_and_content = all_content.split("\n", 2);
        //String title = title_and_content[0];
        //String content = title_and_content[1];

        String stemmedTitle = stopStem.process(title);
        String stemmedContent = stopStem.process(all_content);
        //System.out.println(stemmedTitle);
        //System.out.println(stemmedContent);
        //finish stem process text
        
        //process the stemmed terms
        String[] strArr = stemmedContent.split(" ");
        String[] strTitle = stemmedTitle.split(" ");
        int pos = 0;

        Map<Integer,Integer> map = new HashMap<>();
        List<Integer> outputArray = new ArrayList<>();
        for(int i=0;i<strArr.length;i++){
            //each term
            pos++;
            int tid = get_wordID(strArr[i]);
            addEntry_inv(tid,pid,pos);
            //System.out.println("adding inv "+tid+" "+pid+" "+" "+pos);

            int count = map.getOrDefault(tid,0);
            map.put(tid,count+1);
            outputArray.add(tid);
        }

        // add title to database 5
        int title_pos = 0;
        for(int j=0;j<strTitle.length;j++){
            //each term
            title_pos++;
            int tid = get_wordID(strTitle[j]);
            addEntry_title_inv(tid,pid,title_pos);
            //System.out.println("adding inv "+tid+" "+pid+" "+" "+title_pos);

        }

        SortComparator comp = new SortComparator(map);
        Collections.sort(outputArray,comp);
        //outputArray now have[tid1,tid1,tid1,tid2,tid2,tid3]
        int curtid=-1;
        int curtf=0;
        int remaining = outputArray.size();
        String ff="";
        for(Integer i:outputArray){
            remaining--;
            if(curtid==-1)//first time
                curtid = i;
            if(i==curtid)//old term as before
                curtf++;
            else{//getting into new term
                ff+=(new String(String.valueOf(curtid))+" "+curtf+",");
                curtf = 1;
                curtid = i;
            }
            if(remaining==0){
                ff+=(new String(String.valueOf(curtid))+" "+curtf+",");
                addEntry_forward(pid,ff);
            }
       
        }
        //finish process the stemmed terms
        //finish one loop(finishing processing one url)
        //}catch (MalformedURLException e){ System.err.println(e.toString());}
        }catch (ParserException e){ System.err.println(e.toString());}
        }catch (IOException e){ System.err.println(e.toString());}
        
		}
       


    }


    public static void print (String filename,int num){
        try{
        try{
            FileWriter Writer = new FileWriter(filename +".txt");
            int page_id = 1;
            String content = new String(inv_url.get((new String(String.valueOf(page_id))).getBytes()));
            while (page_id<num+1) {
                byte[] Url_byte = inv_url.get(new String(String.valueOf(page_id)).getBytes());
                byte[] pi = page_info.get(new String(String.valueOf(page_id)).getBytes());
                if(pi!=null){
                String page_info_byte = new String(pi);
                String[] page_info = page_info_byte.split(";");
                String final_content = (page_info[0] + "\n" +new String(Url_byte)+ "\n" + page_info[2] + "\n"+ page_info[1]+ "\n" 
                + "Keyword: (only top 20 keywords are listed)" + "& (tf, df, idf, term_weight)" +"\n");
                System.out.println(new String(String.valueOf(page_id)));             //debug

                byte [] keyword_info = forward.get((new String(String.valueOf(page_id))).getBytes());
                byte [] termweight_info = term_weight.get((new String(String.valueOf(page_id))).getBytes());
                if (termweight_info != null ){
                    String [] keyword_lists = (new String(keyword_info)).split(",");
                    String [] termweight_lists = (new String(termweight_info)).split(",");
                    for(int i = 0; i < keyword_lists.length-1; i++){
                        String [] forward_info = keyword_lists[i].split(" ");                         //split to term_id and frequency(tf)
                        byte [] keyword_byte = inv_word.get(forward_info[0].getBytes());              //transfer term_id to keyword and df
                        String idf_no = new String(idf.get(forward_info[0].getBytes()));              //get the idf 
                        String [] termweight_split = termweight_lists[i].split(" ");   
                        String keyword_df = new String (keyword_byte);
                        String[] keyword = keyword_df.split(" ");
                        //System.out.print( "debug:" + keyword[0] + " " + forward_info[1] +"; " );
                        final_content = (final_content + keyword[0] + ", " + forward_info[1] + ", " + keyword[1] + ", " + idf_no + ", " + termweight_split[1] +"\n");
                        if (i >= 19) {break;}
                    }} else {final_content = final_content+ "no word exist" + "\n";}
                final_content = page_id +"." + final_content + "\n" +"child_links:(only top 20) " +"\n";

                byte [] child_info = parent_child_relation.get((new String(String.valueOf(page_id))).getBytes());  //get the child_id of child
                if (child_info != null) {
                    String child_urls = new String (child_info);            //transfer it to split
                    String [] child_url_sets = child_urls.split(",");       
                    for(int i = 0; i < child_url_sets.length-1; i++){ 
                        byte [] child_bytes = inv_url.get (child_url_sets[i].getBytes());  //transfer each child_id to child_url
                        String child_url = new String (child_bytes);
                        final_content = final_content + child_url + "\n" ;
                        if (i >= 19) {break;}
                    }
                }
                else{final_content = final_content + "no child_list exist" + "\n";}
                final_content = final_content + "---------------------------------------------------------------------------" + "\n";
                Writer.write (final_content);
                //System.out.println(final_content); //write the final content
            
            }
                page_id += 1;
                content = String.valueOf(inv_url.get((String.valueOf(page_id)).getBytes()));
            }
                Writer.flush();
                Writer.close();

        }catch (IOException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();}
        }catch(RocksDBException e)
        { System.err.println(e.toString());}
        }


	public static void main(String[] args)
    {
		 try
        {
		project_database pdb = new project_database();
        pdb.crawl("https://www.cse.ust.hk",100);
        pdb.calculation(100);
        pdb.print("spider_result",100);
		}
        catch(RocksDBException e)
        { System.err.println(e.toString());}

	}

}

class SortComparator implements Comparator<Integer>{
    private final Map<Integer,Integer> freqMap;
    SortComparator(Map<Integer,Integer> tFreqMap){
        this.freqMap = tFreqMap;
    }

    @Override
    public int compare(Integer k1, Integer k2){
        int freqCompare = freqMap.get(k2).compareTo(freqMap.get(k1));
        int valueCompare = k1.compareTo(k2);

        if(freqCompare==0)
            return valueCompare;
        else
            return freqCompare;
    }
}
