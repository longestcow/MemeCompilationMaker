import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VidMemesUploader {
	
	
	static String sub = "discordVideos",time="", len;
	static int n = 120;
	static String ff="C:\\Users\\omiye\\Downloads\\ffmpeg-2023-05-25-git-944243477b-full_build\\bin\\ffmpeg";

	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		
		saveVideos(getVideoURLs(sub)); //save all images to src/memes
		System.out.println("videos retrieved ("+((System.currentTimeMillis()-start)/1000)+"s)");
		if(!makeVideo()) {
			System.out.println("something went wrong (makeVideo() returned false)");
			return;
		}
		System.out.println("videos combined ("+((System.currentTimeMillis()-start)/1000)+"s) (vid len = "+getLen("vids\\"+time+".mp4")+")");
		System.out.println("finished process");
		return;
	}
	
	public static List<String> getVideoURLs(String sub) throws IOException{
		URL jsons = new URL("https://www.reddit.com/r/"+sub+"/top.json?t=week&limit=200&raw_json=1");//get the memes as json objects
		URLConnection con = jsons.openConnection();
		con.setRequestProperty("User-Agent", "Windows 11:com.MemeUploader.MemeUploader:v0.1 (by /u/user_guy_thing)"); //you have to do this so that reddit doesnt freak out
	    JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) con.getContent()));
	    JsonObject rootObj = root.getAsJsonObject(), rootObjs = rootObj;
	    JsonElement afterObj = rootObj.get("data").getAsJsonObject().get("after");
	    String after="aaaa";
	    if(afterObj.toString().equals(null)) {
	    	after=afterObj.getAsString();
	    }
	    
	    JsonArray arr = rootObjs.get("data").getAsJsonObject().get("children").getAsJsonArray(); //gets all the posts in a list
	    List<String> urls = new ArrayList<>();
	    for(int j = 0; j<2; j++) {
		    for(JsonElement json : arr) {
		    	if(json.getAsJsonObject().get("data").getAsJsonObject().get("secure_media").isJsonNull()) 
		    		continue;
		    	String url = json.getAsJsonObject().get("data").getAsJsonObject()
		    			.get("secure_media").getAsJsonObject()
		    			.get("reddit_video").getAsJsonObject()
		    			.get("fallback_url").getAsString();
		    	
		    	if(!url.contains("external-preview")) 
		    		urls.add(url);
		    		
		    
		    }
		    if(after.equals("aaaa"))
		    	return urls;
		    jsons = new URL(jsons.toExternalForm()+"&after="+after);
			con = jsons.openConnection();
			con.setRequestProperty("User-Agent", "Windows 11:com.MemeUploader.MemeUploader:v0.1 (by /u/user_guy_thing)"); //you have to do this so that reddit doesnt freak out
		    root = JsonParser.parseReader(new InputStreamReader((InputStream) con.getContent()));
		    rootObj = root.getAsJsonObject();
		    arr = rootObj.get("data").getAsJsonObject().get("children").getAsJsonArray();		
		    
	    }
	    return urls;

	}
	
	public static void saveVideos(List<String> urls) throws Exception {
	    int i = 0;
	    FileUtils.cleanDirectory(new File("src/memes/"));
	    PrintWriter writer = new PrintWriter("input.txt");
	    writer.print("");
	    writer.close();
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("input.txt", true)));
	    int max = 600;
	    String fname="", fvname = "", faname="", dash="";
	    for(String url : urls) {
	    	dash=url.split("DASH_")[1].split(".mp4")[0];
	    	System.out.println(++i+". "+url);
	    	
	    	fvname="src/memes/vid"+((i<9)?"00":"0")+i+"vid.mp4";
	    	faname="src/memes/vid"+((i<9)?"00":"0")+i+"aud.mp4";
	    	fname="src/memes/vid"+((i<9)?"00":"0")+i+".mp4";
	    	
	    	InputStream inVid = new URL(url).openStream(); 
	    	Files.copy(inVid, new File(fvname).toPath());
	    	InputStream inAud = new URL(url.replace("DASH_"+dash, "DASH_audio")).openStream();
	    	Files.copy(inAud, new File(faname).toPath());
	    	
	    	System.out.println("seperate files saved to memory");
	    	
	    	mergeVidAud(fvname, faname, fname);
	    	
	    	out.println("file '"+fname+"'");
	    	
	    	String len = getLen(fname);
	    	max-=Integer.parseInt(len.split(":")[1])*60 + Integer.parseInt(len.split(":")[2]);
	    	
	    	if(max<=0) 
	    		break;
	    	
	    }
    	out.close();
	}

	public static void mergeVidAud(String vid, String aud, String out) throws Exception{
		
		ProcessBuilder pb = new ProcessBuilder(ff, "-vn", "-i", "\""+aud+"\"", "-an", "-i","\""+vid+"\"", "-acodec", "copy", "-vcodec", "copy", "\""+out+"\"");
		pb.redirectErrorStream();
		Process proc = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String st = null;
		while ((st=reader.readLine()) != null) {System.out.println(st);}
		proc.waitFor();
		System.out.println("merge complete");
		new File(vid).delete();
		new File(aud).delete();
		return;
	}
	
	public static boolean makeVideo() throws IOException, InterruptedException {
		time=(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
		String of="vids\\"+time+".mp4";
		ProcessBuilder pb = new ProcessBuilder(ff,"-f","concat","-safe 0","-i", "input.txt", "-c", "copy", of);
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		while ((reader.readLine()) != null) {}
		return proc.waitFor() == 0;
	}
	
	public static String getLen(String path) throws InterruptedException, IOException {
		ProcessBuilder pb = new ProcessBuilder(ff,"-i","\""+path+"\"");
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		String len="";
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String st=null;
		while ((st=reader.readLine()) != null) {
			if(st.contains("Duration: ") && st.contains(", start: ")) 
				len=(st.split(" ")[3].split("\\.")[0]);
		}
		proc.waitFor();
		return len;
	}

	
}
