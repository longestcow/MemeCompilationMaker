import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MemesUploader {
	
	
	static String sub = "",music="",time="",len="";
	static int n = 100;
	static String ff="src\\ffmpeg";

	
	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner s = new Scanner(System.in);
		long start = System.currentTimeMillis();
		System.out.println("Subreddit name: ");
		sub = s.nextLine();
		System.out.println("Music: ");
		music = s.nextLine();
		saveImages(getImageURLs(sub)); //save all images to src/memes
		System.out.println("images retrieved ("+((System.currentTimeMillis()-start)/1000)+"s)");
		if(!makeVideo()) {
			System.out.println("something went wrong (makeVideo() returned false)");
			return;
		}
		getLen();
		System.out.println("images combined ("+((System.currentTimeMillis()-start)/1000)+"s) (vid len = "+len+")");

		combineFiles();
		System.out.println("finished process ("+((System.currentTimeMillis()-start)/1000)+"s)");
		return;
	}
	
	public static List<String> getImageURLs(String sub) throws IOException{
		URL jsons = new URL("https://www.reddit.com/r/"+sub+"/top.json?t=month&limit=200&raw_json=1");//get the memes as json objects
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
	    int i = n;
	    for(int j = 0; j<2; j++) {
		    for(JsonElement json : arr) {
		    	if(json.getAsJsonObject().get("data").getAsJsonObject().get("media_metadata")!=null) 
		    		continue;
		    	String url = json.getAsJsonObject()
		    			.get("data").getAsJsonObject()
		    			.get("preview").getAsJsonObject()
		    			.get("images").getAsJsonArray()
		    			.get(0).getAsJsonObject()
		    			.get("source").getAsJsonObject()
		    			.get("url").getAsString();
		    	String ext = url.split("\\?")[0].split("\\.")[3];
		    	if(!url.contains("external-preview") && !ext.equals("gif")) {
		    		urls.add(url);
		    		i-=1;
		    	}
		    
		    }
		    if(i==0 || after.equals("aaaa"))
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
	
	public static void saveImages(List<String> urls) throws IOException {
	    int i = 0;
	    FileUtils.cleanDirectory(new File("src/memes/"));
	    PrintWriter writer = new PrintWriter("input.txt");
	    writer.print("");
	    writer.close();
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("input.txt", true)));
	    String fname = "";
	    for(String url : urls) {
	    	
	    	fname="src/memes/img"+((i<9)?"00":"0")+i+".png";
	    	System.out.println(++i+". "+url);

	    	try {
	    	BufferedImage image = ImageIO.read(new URL(url)); 
	    	File file = new File(fname);
	    	if(i==1)
	    		image=scaled(image, 720,720);
	    	ImageIO.write(image, "png", file);
	    	} catch(IIOException e) {continue;}
	    	out.println("file '"+fname+"'");
	    	out.println("duration "+ThreadLocalRandom.current().nextInt(6, 8 + 1));
	    }
    	out.println("file '"+"src/memes/img"+((i--<9)?"00":"0")+i+".png'");
    	out.close();
	}
	
	private static BufferedImage scaled(BufferedImage image, int WIDTH, int HEIGHT) {
		  BufferedImage bi = null;
		    try {
		        bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		        Graphics2D g2d = (Graphics2D) bi.createGraphics();
		        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY));
		        g2d.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
		    } catch (Exception e) {
		        e.printStackTrace();
		        return null;
		    }
		    return bi;		
	}

	public static boolean makeVideo() throws IOException, InterruptedException {
		time=(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
		String of="vids\\"+time+".mp4";
		ProcessBuilder pb = new ProcessBuilder(ff,"-f","concat","-i", "input.txt", "-pix_fmt", "yuv420p", "-vf", "\"pad=ceil(iw/2)*2:ceil(ih/2)*2\"", of);
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		while ((reader.readLine()) != null) {}
		return proc.waitFor() == 0;
	}
	
	public static void getLen() throws InterruptedException, IOException {
		ProcessBuilder pb = new ProcessBuilder(ff,"-i","\"vids\\"+time+".mp4\"");
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String st=null;
		while ((st=reader.readLine()) != null) {
			if(st.contains("Duration: ") && st.contains(", start: ")) 
				len=(st.split(" ")[3].split("\\.")[0]);
		}
		proc.waitFor();
	}



	public static void combineFiles() throws InterruptedException, IOException {
		String of = "vids//"+time;
		String musicc = (music.isEmpty())?"\"music//"+ThreadLocalRandom.current().nextInt(1, 10)+".mp3\"":music;
		System.out.println("music picked: "+musicc);
		ProcessBuilder pb = new ProcessBuilder(ff, "-i", musicc, "-i", "\""+of+".mp4\"", "-acodec", "copy", "-vcodec", "copy", "-shortest", "\""+of+"finished.mp4\"");
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String st = null;
		while ((st=reader.readLine()) != null) {}
		proc.waitFor();
		new File(of+".mp4").delete();
		System.out.println("done");
	}
	
}
