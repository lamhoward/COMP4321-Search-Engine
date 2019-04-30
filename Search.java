/* --
COMP4321 Project Phase 2 Search
Group 11 Member
Xu, Feiting(fxuaf)  20329359
Lam Hon Wa(hwlamad) 20348745
Li, Junze(jlicx)    20413186

*/
package Database;


import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

import java.util.Scanner;

import java.lang.Math;

import java.util.*;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Date;


import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Search{
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
    private static RocksDB query_history;
    private static RocksDB query_result;
    private static RocksDB query_index;

    private static StopStem stopStem;
    private static List<Integer> similarQuery;
    private static double N=1100; //!!!!for phrase IDF calculation, need to change with crawl process
	//end of Data Member

	//Constructor
 	public Search() throws RocksDBException{
 		options = new Options();
        options.setCreateIfMissing(true);
        similarQuery=new ArrayList();
        try
        {
            // a static method that loads the RocksDB C++ library.
            RocksDB.loadLibrary();
            String path_pre = "/home/test/lab4/apache-tomcat-7.0.93/webapps/ROOT/";
            //String path_pre="";
            String path1 = path_pre+"db/url";
            String path2 = path_pre+"db/word";
            String path3 = path_pre+"db/forward";
            String path4 = path_pre+"db/inv";
            String path5 = path_pre+"db/title_inv";
            String path6 = path_pre+"db/parent_child_relation";
            String path7 = path_pre+"db/page_info";
            String path8 = path_pre+"db/idf";
            String path9 = path_pre+"db/history";
            String path10 = path_pre+"db/inv_url";
            String path11 = path_pre+"db/inv_word";
            String path12 = path_pre+"db/child_parent";
            String path13 = path_pre+"db/term_weight";
            String path14 = path_pre+"db/list_keyword";//in alphabet order
            String path15 = path_pre+"db/query_history";
            String path16 = path_pre+"db/query_result";
            String path17 = path_pre+"db/query_index";
           
            // create/open database
            url = RocksDB.open(options,path1); 
            word = RocksDB.open(options,path2);
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
            query_history = RocksDB.open(options,path15);
            query_result = RocksDB.open(options,path16);
            query_index = RocksDB.open(options,path17);
            byte[] max_num = query_history.get("max_id".getBytes());
            if(max_num==null) query_history.put("max_id".getBytes(),(new String(String.valueOf(0)).getBytes()));

        }
        catch(RocksDBException e)
        {
            System.err.println(e.toString());
        }

 	}
 	//end of Constructor
 	
 	//get term id for the term, if term not exist, return -1
 	public static int term_to_term_id(String term) throws RocksDBException
 	{
 		byte[] content = word.get(term.getBytes());
 		if(content!=null){return Integer.parseInt(new String(content));}
 		else{return -1;}
 	}

 	//helper function for int to byte[]
 	public static byte[] iToBA(int i){
 		return (new String(String.valueOf(i)).getBytes());
 	}

    //helper function to transfer a query to a List<Integer> q_tid_array
    private static List<Integer> query_to_tid_array(String q_in) throws RocksDBException
    {try{
        String[] q_split = q_in.split("\"");
        List<String> not_phrase = new ArrayList();
        List<Integer> q_tid_array = new ArrayList(); 

        for (int i=0;i<q_split.length;i++){
            if(q_split[i].isEmpty()||q_split[i].equals(" ")) 
                continue;
            if(i%2==0){//not phrase term
                not_phrase.add(q_split[i]);
            }
            else{//phrase term
                String stem_pharse = StopStem.process(q_split[i]);
                if(stem_pharse.isEmpty()) continue; //no term left
                String[] stem_phrase_split = stem_pharse.split(" ");
                if(stem_pharse.equals(stem_phrase_split[0])){//only one term left
                    not_phrase.add(q_split[i]);
                }
                else{//after stem still a phrase
                    String previous_phrase=stem_phrase_split[0];
                    for(int j=1;j<stem_phrase_split.length;j++){
                        String c_phrase = previous_phrase+" "+stem_phrase_split[j];

                        System.out.println("begin phrase process: "+c_phrase);
                        //start processing the new term(phrase/part of phrase)
                         int ptid = term_to_term_id(previous_phrase);
                         if(ptid==-1) break;
                         int jtid = term_to_term_id(stem_phrase_split[j]);
                         if(jtid==-1) break;
                         int ctid = term_to_term_id(c_phrase);
                         if(ctid!=-1) {
                            previous_phrase=c_phrase;
                            System.out.println("phrase \""+c_phrase+"\" already in db with tid "+ctid);
                            if(j==stem_phrase_split.length-1) q_tid_array.add(ctid);
                            continue;
                         }

                        ctid = Integer.parseInt(new String(word.get("max_id".getBytes())));
                        ctid+=1;
                        word.put("max_id".getBytes(),new String(String.valueOf(ctid)).getBytes());
                        word.put(c_phrase.getBytes(),new String(String.valueOf(ctid)).getBytes());
                       
                        //process title_inv for the new phrase
                        byte[] content_tp = title_inv.get(iToBA(ptid));
                        if(content_tp!=null) {
                            byte[] content_tj = title_inv.get(iToBA(jtid));
                            if(content_tj!=null) {
                                String c_tinv_list="";
                                String[] pr_tinv_list = (new String(content_tp)).split(",");
                                String[] j_tinv_list = (new String(content_tj)).split(",");

                                int j_tinv_pid = -1;
                                int j_tinv_i=-1;
                                int pr_tinv_pid=-1;
                                int j_tinv_last_pid=-1;
                                

                                String t = j_tinv_list[j_tinv_list.length-1];
                                String[] tt = t.split(" ");
                                j_tinv_last_pid=Integer.parseInt(tt[1]);
                                
                                // else{
                                // String t = j_tinv_list[j_tinv_list.length-2];
                                // String[] tt = t.split(" ");
                                // j_tinv_last_pid=Integer.parseInt(tt[1]);
                                // }
                                for(int pr_tinv_i=0;pr_tinv_i<pr_tinv_list.length-1;pr_tinv_i++){
                                    String[] temp = pr_tinv_list[pr_tinv_i].split(" ");
                                    pr_tinv_pid = Integer.parseInt(temp[0]);
                                    int pr_tinv_pos = Integer.parseInt(temp[1]);
                                    if(j_tinv_i!=-1) j_tinv_i--;

                                    do{
                                        j_tinv_i++;
                                        String[] temp_j = j_tinv_list[j_tinv_i].split(" ");
                                        j_tinv_pid = Integer.parseInt(temp_j[0]);
                                        if(pr_tinv_pid<j_tinv_pid) break;
                                        if(pr_tinv_pid>j_tinv_pid) continue;
                                        //pr and j must have same pid now
                                        int j_tinv_pos = Integer.parseInt(temp_j[1]);
                                        if(pr_tinv_pos+1==j_tinv_pos){//phrase is found
                                            c_tinv_list+= (new String(String.valueOf(pr_tinv_pid))+" "
                                                            +new String(String.valueOf(j_tinv_pos))+",");
                                        }
                                        else if(pr_tinv_pos<j_tinv_pos) break;

                                    }while(j_tinv_i<j_tinv_list.length-1);

                                    if(pr_tinv_pid>j_tinv_last_pid) break;
                                }
                                //System.out.println("c_tinv_list: "+c_tinv_list);
                                if(!c_tinv_list.isEmpty()){
                                    title_inv.put(iToBA(ctid),c_tinv_list.getBytes());
                                }
                            }
                        }
                        //finish process title_inv for the new phrase
                        
                        //process inv for new phrase
                        byte[] content1 = inv.get(iToBA(ptid));
                        if(content1==null) continue;
                        byte[] content = inv.get(iToBA(jtid));
                        if(content==null) continue;
                        String[] pr_inv_list = (new String(content1)).split(",");
                        String[] j_inv_list = (new String(content)).split(",");
                        String c_inv_list="";
                        int pr_inv_pid = -1;
                        Map<Integer,Integer> pid_tf_map = new HashMap<>();//for tf
                        int j_inv_pid = -1;
                        int j_inv_i=-1;
                        String t = j_inv_list[j_inv_list.length-1];
                        String[] tt = t.split(" ");
                        int j_inv_last_pid=Integer.parseInt(tt[1]);
                        int df=0;

                        for(int pr_inv_i=0;pr_inv_i<pr_inv_list.length-1;pr_inv_i++){
                            String[] temp_pr_inv_array = pr_inv_list[pr_inv_i].split(" ");
                            pr_inv_pid = Integer.parseInt(temp_pr_inv_array[0]);
                            int pr_inv_pos = Integer.parseInt(temp_pr_inv_array[1]);
                            if(j_inv_i!=-1) j_inv_i--;

                            do{
                                j_inv_i++;
                                String[] temp = j_inv_list[j_inv_i].split(" ");
                                j_inv_pid = Integer.parseInt(temp[0]);
                                if(pr_inv_pid<j_inv_pid) break;
                                if(pr_inv_pid>j_inv_pid) continue;
                                //pr and j must have same pid now
                                int j_inv_pos = Integer.parseInt(temp[1]);
                                if(pr_inv_pos+1==j_inv_pos){//phrase is found
                                    c_inv_list+= (new String(String.valueOf(pr_inv_pid))+" "+new String(String.valueOf(j_inv_pos))+",");
                                    int count_pid_tf = pid_tf_map.getOrDefault(pr_inv_pid,0);
                                    pid_tf_map.put(pr_inv_pid,count_pid_tf+1);
                                }
                                else if(pr_inv_pos<j_inv_pos) break;

                            }while(j_inv_i<j_inv_list.length-1);

                            if(pr_inv_pid>j_inv_last_pid) break;
                        }
                       // System.out.println("c_inv_list: "+c_inv_list);
                        if(!c_inv_list.isEmpty()){
                            inv.put(iToBA(ctid),c_inv_list.getBytes());
                            //finish process inv for new phrase
                            //start calculation for new phrase
                            for (Integer key_pid : pid_tf_map.keySet()) {
                                df++;
                            }
                            //1. store df
                            //System.out.println("store df: "+df);
                             inv_word.put(iToBA(ctid),
                                (c_phrase+" "+ new String(String.valueOf(df))).getBytes()); 

                            //2. calculate idf
                            
                            double idf_no = (Math.log(N/df)/Math.log(2));                                   //calculate the idf
                            idf_no = Math.floor(idf_no*1000)/1000;
                            idf.put (iToBA(ctid), (String.valueOf(idf_no)).getBytes());

                            //3. store tf and term weight
                            Iterator<Map.Entry<Integer, Integer>> entries = pid_tf_map.entrySet().iterator();
                            while (entries.hasNext()) {
                                Map.Entry<Integer, Integer> entry = entries.next();
                                int map_pid = (entry.getKey()).intValue();
                                int map_tf = (entry.getValue()).intValue();
                                byte[] content_f = forward.get(iToBA(map_pid));
                                String forward_string = new String(content_f);
                                String newContent = forward_string + new String(String.valueOf(ctid))+" "
                                                    +new String(String.valueOf(map_tf))+",";
                                forward.put(iToBA(map_pid),newContent.getBytes());
                                //term weight
                                String[] forward_array=forward_string.split(",");
                                //System.out.println("pid: "+map_pid);
                                
                                String[] max_term_id = forward_array[0].split(" ");                             //get the max number of term ids
                                int max_tf_pid = Integer.parseInt(max_term_id[1]);
                                
                                double term_weight_no = (map_tf * idf_no /max_tf_pid);
                                term_weight_no = Math.floor(term_weight_no*1000)/1000;
                                //System.out.println("new_tw: "+term_weight_no);
                                byte[] content_tw = term_weight.get(iToBA(map_pid));
                                String tw_string = new String(content_tw);
                                newContent = tw_string + new String(String.valueOf(ctid))+" "
                                                    +new String(String.valueOf(term_weight_no))+",";
                                //System.out.println("term weight content: "+newContent);
                                term_weight.put(iToBA(map_pid),newContent.getBytes());
                            }

                            //finish calculation for new phrase
                        }
                        
                        //finish processing the new term(phrase/part of phrase)
                        previous_phrase=c_phrase;
                        System.out.println("finish phrase process: "+c_phrase);
                        if(j==stem_phrase_split.length-1) q_tid_array.add(ctid);
                    }
                //finish the whole phrase process
                }
            }
            
            System.out.println("Query Term "+i+" "+q_split[i]);
        }

        //stopstem not_phrase
        String not_phrase_q = "";
        for(int ni=0;ni<not_phrase.size();ni++){
            not_phrase_q +=" ";
            not_phrase_q +=not_phrase.get(ni);
            
        }
        String not_phrase_q_stem =StopStem.process(not_phrase_q);
        System.out.println("not phrase part: "+not_phrase_q_stem);

        //finish stopstem not_phrase

        
        String[] q_terms = not_phrase_q_stem.split(" ");
        for(int q_terms_i=0;q_terms_i<q_terms.length;q_terms_i++){
            int tid = term_to_term_id(q_terms[q_terms_i]);
            if(tid!=-1) q_tid_array.add(tid);
        }
        return q_tid_array;
    }catch(RocksDBException e)
        { System.err.println(e.toString()); return null;}
    }

    public static String[][] query(String q_in,int num_output) throws RocksDBException
    {   
        similarQuery = new ArrayList();
        if(q_in=="") return null;
        byte[] content = query_index.get(q_in.getBytes());
        if(content==null){//the query is new
        	//store the query in history
	        int num_page = Integer.parseInt(new String(query_history.get("max_id".getBytes())));
	        num_page+=1;
	        query_history.put("max_id".getBytes(),new String(String.valueOf(num_page)).getBytes());
	        query_history.put(iToBA(num_page),q_in.getBytes());
	 	    query_index.put(q_in.getBytes(),iToBA(num_page));

	        //process the query
	        List<Integer> q_tid_array = query_to_tid_array(q_in); 
	        return query_helper(q_tid_array,num_output,q_in);
    	}
        else{//the query is already in query_history, could get result directly
            String q_index = new String(content);
            
            byte[] content_q_result = query_result.get(q_index.getBytes());
            if(content_q_result==null) return null;
            String q_result = new String(content_q_result);
            if(q_result==""||q_result==" "||q_result==null) return null;
    		String[] q_result_array = q_result.split(",");

    		String[][] results= new String[num_output][25];
        	Map<Integer,Integer> tid_freq_map = new HashMap<>();
        

        for(int i=0;i<num_output;i++){
        	//System.out.println("q_result_array.length:"+q_result_array.length);
            if(i>=q_result_array.length) break;
            String[] pid_score_arr = q_result_array[i].split(" ");
            if(pid_score_arr[0].equals("")||pid_score_arr[0].equals("\n")||pid_score_arr[0]==null) return null;
            int page_id = Integer.parseInt(pid_score_arr[0]);
            results[i][0]=pid_score_arr[1]; //score
            

            byte[] Url_byte = inv_url.get(new String(String.valueOf(page_id)).getBytes());
            results[i][2] = new String(Url_byte); //url

            byte[] pi = page_info.get(new String(String.valueOf(page_id)).getBytes());
            if(pi!=null){
                String page_info_byte = new String(pi);
                String[] page_info = page_info_byte.split(";");
                results[i][1] = page_info[0];//title
                results[i][3] = page_info[2];//last modified date
                results[i][4] = page_info[1];//size
                
                //keyword part
                byte [] keyword_info = forward.get((new String(String.valueOf(page_id))).getBytes());      
                if(keyword_info!=null){
                String [] keyword_lists = (new String(keyword_info)).split(",");
                for(int j = 0; j < keyword_lists.length; j++){
                    String [] forward_info = keyword_lists[j].split(" ");
                    int tid_f = Integer.valueOf(forward_info[0]);
                    byte [] keyword_byte = inv_word.get(forward_info[0].getBytes());              //transfer term_id to keyword and df
                    String keyword_df = new String (keyword_byte);
                    String[] keyword = keyword_df.split(" ");
                    results[i][5+2*j]=keyword[0];
                    results[i][6+2*j]=forward_info[1];
                    int tid_freq_count = tid_freq_map.getOrDefault(tid_f,0);
                    tid_freq_count+= Integer.valueOf(forward_info[1]);
                    tid_freq_map.put(tid_f,tid_freq_count);
                    if (j >= 4) {break;}
                }
                }
                //finish keyword


                //parent link
                byte [] parent_info = child_parent.get(iToBA(page_id));  //get the p_id of parent
                if (parent_info != null) {
                    String parent_urls = new String (parent_info);            //transfer it to split
                    String [] parent_url_sets = parent_urls.split(",");       
                    for(int j = 0; j < parent_url_sets.length-1; j++){
                        byte [] parent_bytes = inv_url.get (parent_url_sets[j].getBytes());  //transfer each id to url
                        String parent_url = new String (parent_bytes);
                        results[i][15+j] = parent_url;
                        if(j>=4) break;
                    }
                }
                //finish parent link
                
                //child link
                byte [] child_info = parent_child_relation.get((new String(String.valueOf(page_id))).getBytes());  //get the child_id of child
                if (child_info != null) {
                    String child_urls = new String (child_info);            //transfer it to split
                    String [] child_url_sets = child_urls.split(",");       
                    for(int j = 0; j < child_url_sets.length-1; j++){
                        byte [] child_bytes = inv_url.get (child_url_sets[j].getBytes());  //transfer each child_id to child_url
                        String child_url = new String (child_bytes);
                        results[i][20+j] = child_url;
                        if(j>=4) break;
                    }
                }
                //finish child link
                 }

                 
        

 	     }
                //store the similarQuery
                List<Integer> tid_freq_rank=new ArrayList<>();
                for(int key:tid_freq_map.keySet()){
                    tid_freq_rank.add(key);
                }
                SortComparator comp = new SortComparator(tid_freq_map);
                Collections.sort(tid_freq_rank,comp);

                 int similarQuery_i =5;
                 similarQuery = new ArrayList();
                 for(Integer tid_i:tid_freq_rank){
                    if(similarQuery_i==0) break;
                    similarQuery.add(tid_i);
                    similarQuery_i--;
                    //System.out.println("similarQuery_i"+similarQuery_i);
                 }

                //finish store the similar Query
               
    			return results;
    	}
    }

 	//input a query_tid_array and return the ranked page_id with info in the string array
 	public static String[][] query_helper(List<Integer> q_tid_array,int num_output,String q_in) throws RocksDBException
 	{	
        if(q_tid_array.isEmpty()) return null;
        int q_length = q_tid_array.size();
        
 		//System.out.println("query length: "+q_length);
 		//|Q|
 		double Q = Math.pow(q_length,0.5);
 		//System.out.println("|Q|: "+Q);
 		
        Map<Integer,Double> pid_score = new HashMap<>();
        List<Integer> rank_id = new ArrayList<>();

 		for(Integer itid: q_tid_array){
 			//process each term in the query
            int tid = itid.intValue();
            //System.out.println("itid: "+itid);
 				//search the inv
 				byte[] content = inv.get(iToBA(tid));
 				if(content!=null){
 					String[] inv_list = (new String(content)).split(",");
 					int cpid = -1;
 					for(int j=0;j<inv_list.length-1;j++){
 						String[] pid_pos = inv_list[j].split(" ");
 						if((Integer.parseInt(pid_pos[0]))!=cpid){//find a new pid
 							cpid = (Integer.parseInt(pid_pos[0]));
 							double score = pid_score.getOrDefault(cpid,0.0);
                             byte[] pid_weight = term_weight.get(iToBA(cpid));
                             if(pid_weight!=null){
 							String[] tid_weight=(new String(pid_weight)).split(",");
 							for(int k=0;k<tid_weight.length;k++){
 								String[] t_w = tid_weight[k].split(" ");								
 								if(Integer.parseInt(t_w[0])==tid){               
                                    score = score + Double.parseDouble(t_w[1]);
 									break;
 								}
 							} 
                            score = Math.floor(score*1000)/1000;
 							pid_score.put(cpid,score);
                            //System.out.println("cpid: "+cpid+" score: "+score);
                        }
 						}
 					}
 				}
 			} 		
 		
        //calculate cosine similarity
        for(int key:pid_score.keySet()){
            byte[] content = page_info.get(iToBA(key));
            if(content!=null){
                String pi = new String(content);
                String[] spi = pi.split(";");
                double D = Double.parseDouble(spi[3]);
                
                double score = pid_score.getOrDefault(key,0.0);
                score = score / (D*Q);
                score = Math.floor(score*1000)/1000;
                pid_score.put(key,score);
            }
        }
        

            
            
        //search the title_inv,add 0.05 directly in the score for title term match once
        for(Integer itid: q_tid_array){
            //process each term in the query
            int tid = itid.intValue();
            if(tid==-1){continue;}//the term not exist in db
            else{//the term exist in db
                //System.out.println("processing query term for title: "+q_terms[i]+" "+tid);
                
                //search the title_inv
                byte[] content = title_inv.get(iToBA(tid));
                if(content!=null){
                    String[] inv_list = (new String(content)).split(",");
                    for(int j=0;j<inv_list.length;j++){
                        String[] pid_pos = inv_list[j].split(" ");
                            int cpid = (Integer.parseInt(pid_pos[0]));
                            double score = pid_score.getOrDefault(cpid,0.0);
                            score += 0.1;
                            score = Math.floor(score*1000)/1000;
                            pid_score.put(cpid,score);
                        }
                    }
                }
        }


        //add all the pid from map to the return array and sort the array by score in map
        List<Map.Entry<Integer,Double>> list= new LinkedList<Map.Entry<Integer,Double>>(pid_score.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer,Double>>() {
            public int compare(Map.Entry<Integer,Double> o1, Map.Entry<Integer,Double> o2){
                return -(o1.getValue()).compareTo(o2.getValue());
            }
       
       });

        HashMap<Integer,Double> temp = new LinkedHashMap<Integer,Double>();
        for(Map.Entry<Integer,Double>aa:list){
            temp.put(aa.getKey(),aa.getValue());
        }
        pid_score = temp;
        for(int key:pid_score.keySet()){
            rank_id.add(key);
        }


        //start printing result
        String[][] results= new String[num_output][25];
        Map<Integer,Integer> tid_freq_map = new HashMap<>();
        String query_result_string="";

        for(int i=0;i<num_output;i++){
            if(i>=rank_id.size()) break;
            int page_id = rank_id.get(i);
            System.out.println(page_id+" "+pid_score.getOrDefault(page_id,0.0));
            results[i][0]=String.valueOf(pid_score.getOrDefault(page_id,0.0)); //score
            query_result_string += (new String(String.valueOf(page_id))+" "+results[i][0]+",");

            byte[] Url_byte = inv_url.get(new String(String.valueOf(page_id)).getBytes());
            results[i][2] = new String(Url_byte); //url

            byte[] pi = page_info.get(new String(String.valueOf(page_id)).getBytes());
            if(pi!=null){
                String page_info_byte = new String(pi);
                String[] page_info = page_info_byte.split(";");
                results[i][1] = page_info[0];//title
                results[i][3] = page_info[2];//last modified date
                results[i][4] = page_info[1];//size
                
                //keyword part
                byte [] keyword_info = forward.get((new String(String.valueOf(page_id))).getBytes());      
                if(keyword_info!=null){
                String [] keyword_lists = (new String(keyword_info)).split(",");
                for(int j = 0; j < keyword_lists.length; j++){
                    String [] forward_info = keyword_lists[j].split(" ");
                    int tid_f = Integer.valueOf(forward_info[0]);
                    byte [] keyword_byte = inv_word.get(forward_info[0].getBytes());              //transfer term_id to keyword and df
                    String keyword_df = new String (keyword_byte);
                    String[] keyword = keyword_df.split(" ");
                    results[i][5+2*j]=keyword[0];
                    results[i][6+2*j]=forward_info[1];
                    int tid_freq_count = tid_freq_map.getOrDefault(tid_f,0);
                    tid_freq_count+= Integer.valueOf(forward_info[1]);
                    tid_freq_map.put(tid_f,tid_freq_count);
                    if (j >= 4) {break;}
                }
                }
                //finish keyword


                //parent link
                byte [] parent_info = child_parent.get(iToBA(page_id));  //get the p_id of parent
                if (parent_info != null) {
                    String parent_urls = new String (parent_info);            //transfer it to split
                    String [] parent_url_sets = parent_urls.split(",");       
                    for(int j = 0; j < parent_url_sets.length-1; j++){
                        byte [] parent_bytes = inv_url.get (parent_url_sets[j].getBytes());  //transfer each id to url
                        String parent_url = new String (parent_bytes);
                        results[i][15+j] = parent_url;
                        if(j>=4) break;
                    }
                }
                //finish parent link
                
                //child link
                byte [] child_info = parent_child_relation.get((new String(String.valueOf(page_id))).getBytes());  //get the child_id of child
                if (child_info != null) {
                    String child_urls = new String (child_info);            //transfer it to split
                    String [] child_url_sets = child_urls.split(",");       
                    for(int j = 0; j < child_url_sets.length-1; j++){
                        byte [] child_bytes = inv_url.get (child_url_sets[j].getBytes());  //transfer each child_id to child_url
                        String child_url = new String (child_bytes);
                        results[i][20+j] = child_url;
                        if(j>=4) break;
                    }
                }
                //finish child link
                 }

                 
        

 	     }
                //store the similarQuery
                List<Integer> tid_freq_rank=new ArrayList<>();
                for(int key:tid_freq_map.keySet()){
                    tid_freq_rank.add(key);
                }
                SortComparator comp = new SortComparator(tid_freq_map);
                Collections.sort(tid_freq_rank,comp);

                 int similarQuery_i =5;
                 
                 for(Integer tid_i:tid_freq_rank){
                    if(similarQuery_i==0) break;
                    similarQuery.add(tid_i);
                    similarQuery_i--;
                    //System.out.println("similarQuery_i"+similarQuery_i);
                 }

                //finish store the similar Query
                //store the query_result
                if(q_in!=null){
                byte[] qi_content = query_index.get(q_in.getBytes());
                String index_q = new String(qi_content);
                query_result.put(index_q.getBytes(),query_result_string.getBytes());
            	}
                //finish store the query_result
    return results;
 }
    
    public static String[][] similarPage() throws RocksDBException{
        try{
            if(similarQuery.isEmpty()) return null;
        return query_helper(similarQuery,50,null);
        }catch(RocksDBException e)
        { System.err.println(e.toString()); return null;}


    }

    public static String[] print_keyword() throws RocksDBException{
        try{
        String[] str_array = new String[26];
        for(char i ='a';i<='z';i++){
            String ss = Character.toString(i);
            byte[] content_lk = list_keyword.get(ss.getBytes());
            if(content_lk!=null){
                str_array[i-'a'] = new String(content_lk);
            }
        }
        return str_array;
        }catch(RocksDBException e)
        { System.err.println(e.toString()); return null;}
    }

    public static int num_query() throws RocksDBException{
    	try{
    		byte[] content = query_history.get("max_id".getBytes());
    		String s = new String(content);
    		return Integer.valueOf(s);
    	}catch(RocksDBException e)
        { System.err.println(e.toString()); return -1;}
    }

    public static String[] print_query_history() throws RocksDBException{
    	try{
    		int num = num_query();
    		String[] query_history_array = new String[num];
    		for(int i=1;i<=num;i++){
    			byte[] content = query_history.get(iToBA(i));
    			query_history_array[i-1]=new String(content);
    		}
    		return query_history_array;
    	}catch(RocksDBException e)
        { System.err.println(e.toString()); return null;}
    }

	public static void main(String[] args)
    {
		 try
        {
		Search s=new Search();
        Scanner scan = new Scanner(System.in);

        while(true){
        System.out.println("Select the option:"
                        +"\n1: List of keywords with document frequency>10"
                        +"\n2: Enter a query(double quotes to quote the phrase)"
                        +"\n3: Print past query history");
        String caseno = scan.nextLine();
        
        switch(caseno){
        case "1":
        String [] list_keywords = s.print_keyword();
        for(char i ='a';i<='z';i++){
            System.out.println(i+": "+list_keywords[i-'a']);
        }
        break;
        
        case "2":
        System.out.println("Enter the query please: ");
        String q = scan.nextLine();
		
        String[][]results = s.query(q,5);
         System.out.println("====query is "+q);
         if(results!=null){
        for(int i=0;i<5;i++){
            System.out.println("==============printing result"+i);            
            for(int j=0;j<25;j++){
                System.out.println(j+" "+results[i][j]);
            }
        }}
        else{System.out.println("No result");}
        

        System.out.println("Want Similar Page? Enter y to get similar page. Enter other things to return to main menu");
        String bo = scan.nextLine();

        switch(bo){
	        case "y":
	        System.out.println("====similar page comes from tid: "+similarQuery);
	        String[][] SimilarResults = similarPage(); 
            if(SimilarResults!=null){
	        for(int i=0;i<5;i++){
	            System.out.println("==============printing similar result"+i);
	            for(int j=0;j<25;j++){
	                System.out.println(j+" "+SimilarResults[i][j]);
	            }
	        }}
            else{System.out.println("No result");}
	        default: break;
    	}
        break;
        case "3":
        	int num_q = num_query();
        	if(num_q==0){
        		System.out.println("No query history yet");
        	}
        	else{
        		System.out.println("Total number of query history: " + num_q);
        		String[] q_history_array = print_query_history();
        		for(int i=0;i<num_q;i++){
        			System.out.println("History "+(i+1)+": "+q_history_array[i]);
        		}
        	}
        break;
        default:
        System.out.println("Invalid input. Please try again");
		}
        }
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


class StopStem
{
    private Porter porter;
    private java.util.HashSet stopWords;
    public boolean isStopWord(String str)
    {
        return stopWords.contains(str); 
    }
    
    public static boolean isNumeric(String str){
        for(int i = 0; i<str.length(); i++){
            if(!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    public StopStem(String str)
    {
        super();
        porter = new Porter();
        stopWords = new java.util.HashSet();
                
        // stopWords.add("is");
        // stopWords.add("am");
        // stopWords.add("are");
        // stopWords.add("was");
        // stopWords.add("were");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(str));
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String stem(String str)
    {
        return porter.stripAffixes(str);
    }
    
    public static String process(String rawContent)
    {
        StopStem stopStem = new StopStem("stopwords.txt");
        String stemmed_content = "";
        String[] lines = rawContent.split(System.getProperty("line.separator"));
        for(String tempLine : lines){
            String[] words = tempLine.split(" ");
            for(String word : words){
            if (!stopStem.isStopWord(word) && !stopStem.isNumeric(word)){
         String pre = stopStem.stem(word);
         if (!stopStem.isStopWord(pre)){
            if(stemmed_content!="") stemmed_content += " ";
                stemmed_content += stopStem.stem(word);
      }}
            }
        }
        return stemmed_content;
    }
}


class NewString {
  public String str;

  NewString() {
     str = "";
  }
}

class Porter {

  private String Clean( String str ) {
     int last = str.length();
     
     Character ch = new Character( str.charAt(0) );
     String temp = "";

     for ( int i=0; i < last; i++ ) {
         if ( ch.isLetterOrDigit( str.charAt(i) ) )
            temp += str.charAt(i);
     }
   
     return temp;
  } //clean
 
  private boolean hasSuffix( String word, String suffix, NewString stem ) {

     String tmp = "";

     if ( word.length() <= suffix.length() )
        return false;
     if (suffix.length() > 1) 
        if ( word.charAt( word.length()-2 ) != suffix.charAt( suffix.length()-2 ) )
           return false;
  
     stem.str = "";

     for ( int i=0; i<word.length()-suffix.length(); i++ )
         stem.str += word.charAt( i );
     tmp = stem.str;

     for ( int i=0; i<suffix.length(); i++ )
         tmp += suffix.charAt( i );

     if ( tmp.compareTo( word ) == 0 )
        return true;
     else
        return false;
  }

  private boolean vowel( char ch, char prev ) {
     switch ( ch ) {
        case 'a': case 'e': case 'i': case 'o': case 'u': 
          return true;
        case 'y': {

          switch ( prev ) {
            case 'a': case 'e': case 'i': case 'o': case 'u': 
              return false;

            default: 
              return true;
          }
        }
        
        default : 
          return false;
     }
  }

  private int measure( String stem ) {
    
    int i=0, count = 0;
    int length = stem.length();

    while ( i < length ) {
       for ( ; i < length ; i++ ) {
           if ( i > 0 ) {
              if ( vowel(stem.charAt(i),stem.charAt(i-1)) )
                 break;
           }
           else {  
              if ( vowel(stem.charAt(i),'a') )
                break; 
           }
       }

       for ( i++ ; i < length ; i++ ) {
           if ( i > 0 ) {
              if ( !vowel(stem.charAt(i),stem.charAt(i-1)) )
                  break;
              }
           else {  
              if ( !vowel(stem.charAt(i),'?') )
                 break;
           }
       } 
      if ( i < length ) {
         count++;
         i++;
      }
    } //while
    
    return(count);
  }

  private boolean containsVowel( String word ) {

     for (int i=0 ; i < word.length(); i++ )
         if ( i > 0 ) {
            if ( vowel(word.charAt(i),word.charAt(i-1)) )
               return true;
         }
         else {  
            if ( vowel(word.charAt(0),'a') )
               return true;
         }
        
     return false;
  }

  private boolean cvc( String str ) {
     int length=str.length();

     if ( length < 3 )
        return false;
    
     if ( (!vowel(str.charAt(length-1),str.charAt(length-2)) )
        && (str.charAt(length-1) != 'w') && (str.charAt(length-1) != 'x') && (str.charAt(length-1) != 'y')
        && (vowel(str.charAt(length-2),str.charAt(length-3))) ) {

        if (length == 3) {
           if (!vowel(str.charAt(0),'?')) 
              return true;
           else
              return false;
        }
        else {
           if (!vowel(str.charAt(length-3),str.charAt(length-4)) ) 
              return true; 
           else
              return false;
        } 
     }   
  
     return false;
  }

  private String step1( String str ) {
 
     NewString stem = new NewString();

     if ( str.charAt( str.length()-1 ) == 's' ) {
        if ( (hasSuffix( str, "sses", stem )) || (hasSuffix( str, "ies", stem)) ){
           String tmp = "";
           for (int i=0; i<str.length()-2; i++)
               tmp += str.charAt(i);
           str = tmp;
        }
        else {
           if ( ( str.length() == 1 ) && ( str.charAt(str.length()-1) == 's' ) ) {
              str = "";
              return str;
           }
           if ( str.charAt( str.length()-2 ) != 's' ) {
              String tmp = "";
              for (int i=0; i<str.length()-1; i++)
                  tmp += str.charAt(i);
              str = tmp;
           }
        }  
     }

     if ( hasSuffix( str,"eed",stem ) ) {
           if ( measure( stem.str ) > 0 ) {
              String tmp = "";
              for (int i=0; i<str.length()-1; i++)
                  tmp += str.charAt( i );
              str = tmp;
           }
     }
     else {  
        if (  (hasSuffix( str,"ed",stem )) || (hasSuffix( str,"ing",stem )) ) { 
           if (containsVowel( stem.str ))  {

              String tmp = "";
              for ( int i = 0; i < stem.str.length(); i++)
                  tmp += str.charAt( i );
              str = tmp;
              if ( str.length() == 1 )
                 return str;

              if ( ( hasSuffix( str,"at",stem) ) || ( hasSuffix( str,"bl",stem ) ) || ( hasSuffix( str,"iz",stem) ) ) {
                 str += "e";
           
              }
              else {   
                 int length = str.length(); 
                 if ( (str.charAt(length-1) == str.charAt(length-2)) 
                    && (str.charAt(length-1) != 'l') && (str.charAt(length-1) != 's') && (str.charAt(length-1) != 'z') ) {
                     
                    tmp = "";
                    for (int i=0; i<str.length()-1; i++)
                        tmp += str.charAt(i);
                    str = tmp;
                 }
                 else
                    if ( measure( str ) == 1 ) {
                       if ( cvc(str) ) 
                          str += "e";
                    }
              }
           }
        }
     }

     if ( hasSuffix(str,"y",stem) ) 
        if ( containsVowel( stem.str ) ) {
           String tmp = "";
           for (int i=0; i<str.length()-1; i++ )
               tmp += str.charAt(i);
           str = tmp + "i";
        }
     return str;  
  }

  private String step2( String str ) {

     String[][] suffixes = { { "ational", "ate" },
                                    { "tional",  "tion" },
                                    { "enci",    "ence" },
                                    { "anci",    "ance" },
                                    { "izer",    "ize" },
                                    { "iser",    "ize" },
                                    { "abli",    "able" },
                                    { "alli",    "al" },
                                    { "entli",   "ent" },
                                    { "eli",     "e" },
                                    { "ousli",   "ous" },
                                    { "ization", "ize" },
                                    { "isation", "ize" },
                                    { "ation",   "ate" },
                                    { "ator",    "ate" },
                                    { "alism",   "al" },
                                    { "iveness", "ive" },
                                    { "fulness", "ful" },
                                    { "ousness", "ous" },
                                    { "aliti",   "al" },
                                    { "iviti",   "ive" },
                                    { "biliti",  "ble" }};
     NewString stem = new NewString();

     
     for ( int index = 0 ; index < suffixes.length; index++ ) {
         if ( hasSuffix ( str, suffixes[index][0], stem ) ) {
            if ( measure ( stem.str ) > 0 ) {
               str = stem.str + suffixes[index][1];
               return str;
            }
         }
     }

     return str;
  }

  private String step3( String str ) {

        String[][] suffixes = { { "icate", "ic" },
                                       { "ative", "" },
                                       { "alize", "al" },
                                       { "alise", "al" },
                                       { "iciti", "ic" },
                                       { "ical",  "ic" },
                                       { "ful",   "" },
                                       { "ness",  "" }};
        NewString stem = new NewString();

        for ( int index = 0 ; index<suffixes.length; index++ ) {
            if ( hasSuffix ( str, suffixes[index][0], stem ))
               if ( measure ( stem.str ) > 0 ) {
                  str = stem.str + suffixes[index][1];
                  return str;
               }
        }
        return str;
  }

  private String step4( String str ) {
        
     String[] suffixes = { "al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment", "ent", "sion", "tion",
                           "ou", "ism", "ate", "iti", "ous", "ive", "ize", "ise"};
     
     NewString stem = new NewString();
        
     for ( int index = 0 ; index<suffixes.length; index++ ) {
         if ( hasSuffix ( str, suffixes[index], stem ) ) {
           
            if ( measure ( stem.str ) > 1 ) {
               str = stem.str;
               return str;
            }
         }
     }
     return str;
  }

  private String step5( String str ) {

     if ( str.charAt(str.length()-1) == 'e' ) { 
        if ( measure(str) > 1 ) {/* measure(str)==measure(stem) if ends in vowel */
           String tmp = "";
           for ( int i=0; i<str.length()-1; i++ ) 
               tmp += str.charAt( i );
           str = tmp;
        }
        else
           if ( measure(str) == 1 ) {
              String stem = "";
              for ( int i=0; i<str.length()-1; i++ ) 
                  stem += str.charAt( i );

              if ( !cvc(stem) )
                 str = stem;
           }
     }
     
     if ( str.length() == 1 )
        return str;
     if ( (str.charAt(str.length()-1) == 'l') && (str.charAt(str.length()-2) == 'l') && (measure(str) > 1) )
        if ( measure(str) > 1 ) {/* measure(str)==measure(stem) if ends in vowel */
           String tmp = "";
           for ( int i=0; i<str.length()-1; i++ ) 
               tmp += str.charAt( i );
           str = tmp;
        } 
     return str;
  }

  private String stripPrefixes ( String str) {

     String[] prefixes = { "kilo", "micro", "milli", "intra", "ultra", "mega", "nano", "pico", "pseudo"};

     int last = prefixes.length;
     for ( int i=0 ; i<last; i++ ) {
         if ( str.startsWith( prefixes[i] ) ) {
            String temp = "";
            for ( int j=0 ; j< str.length()-prefixes[i].length(); j++ )
                temp += str.charAt( j+prefixes[i].length() );
            return temp;
         }
     }
     
     return str;
  }


  private String stripSuffixes( String str ) {

     str = step1( str );
     if ( str.length() >= 1 )
        str = step2( str );
     if ( str.length() >= 1 )
        str = step3( str );
     if ( str.length() >= 1 )
        str = step4( str );
     if ( str.length() >= 1 )
        str = step5( str );
 
     return str; 
  }


  public String stripAffixes( String str ) {

    str = str.toLowerCase();
    str = Clean(str);
  
    if (( str != "" ) && (str.length() > 2)) {
       str = stripPrefixes(str);

       if (str != "" ) 
          str = stripSuffixes(str);

    }   

    return str;
    } //stripAffixes

} //class



