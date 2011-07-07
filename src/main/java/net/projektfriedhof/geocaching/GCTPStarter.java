package net.projektfriedhof.geocaching;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GCTPStarter {

	 private DefaultHttpClient httpclient = new DefaultHttpClient();
	
	 private final String host = "http://www.geocaching.com";
	 private final String path = "/login/default.aspx";
	 
	 private String viewstate = "";
     private String viewstate1 = null;
	 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String usage = null;
		if(args.length != 3){
			System.out.println("Usage: java -jar xxxx.jar user pass file_with_gccodes");
			System.exit(0);
		}
		new GCTPStarter().run(args);
	}

	public void run(String[] args){
		
		
		try {
			doLogin(args[0], args[1]);
			checkLogin();
			List<GCData> caches = loadCaches(args[2]);
			buildKMLFromList(caches);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}

	private void buildKMLFromList(List<GCData> caches) {
		StringBuilder out = new StringBuilder();
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
		out.append("<Document>");
		for (GCData gcData : caches) {
			
			final Pattern patternLatlon = Pattern.compile("([NS])[^\\d]*(\\d+)[^°]*° (\\d+)\\.(\\d+) ([WE])[^\\d]*(\\d+)[^°]*° (\\d+)\\.(\\d+)", Pattern.CASE_INSENSITIVE);
			final Matcher matcherLatlon = patternLatlon.matcher(gcData.getCoord());
			matcherLatlon.find();
			int latNegative = -1;
			int lonNegative = -1;
			if (matcherLatlon.group(1).equalsIgnoreCase("N")) {
				latNegative = 1;
			}
			if (matcherLatlon.group(5).equalsIgnoreCase("E")) {
				lonNegative = 1;
			}
			Double lat = new Double(latNegative * (new Float(matcherLatlon.group(2)) + new Float(matcherLatlon.group(3) + "." + matcherLatlon.group(4)) / 60));
			Double lon =new Double(lonNegative * (new Float(matcherLatlon.group(6)) + new Float(matcherLatlon.group(7) + "." + matcherLatlon.group(8)) / 60));
			
			out.append("<Placemark>");
			out.append("<name>" + gcData.getGcCode() +" " + gcData.getName() + "</name>");
			out.append("<description>" + gcData.getCoord() + "</description>");
			out.append("<Point><coordinates>"+lon+","+lat+"</coordinates></Point>");
			out.append("</Placemark>");
		}
		out.append("</Document>");
		out.append("</kml>");
		
		System.out.println(out.toString());
	}

	private void checkLogin() {
		System.out.println("No loginChecking yet");
	}

	private void doLogin(String usr, String pass) throws Exception{
		
		//Find viewState
        HttpGet httpget = new HttpGet(host+path);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        String loginPageString = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        Document loginPage = Jsoup.parse(loginPageString);
        viewstate = loginPage.select("#__VIEWSTATE").first().val();
        //viewstate1 = loginPage.select("#__VIEWSTATE1").first().val();
        
        
        //FireLogin
        HttpPost httpost = new HttpPost(host+path);
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("__EVENTTARGET", ""));
 		nvps.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
 		nvps.add(new BasicNameValuePair("__VIEWSTATE", viewstate));
 		if (viewstate1 != null) {
 			nvps.add(new BasicNameValuePair("__VIEWSTATE1", viewstate1));
 			nvps.add(new BasicNameValuePair("__VIEWSTATEFIELDCOUNT", "2"));
 		}
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$tbUsername", usr));
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$tbPassword", pass));
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$cbRememberMe", "on"));
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$btnSignIn", "Login"));
         
        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();
        EntityUtils.consume(entity);
          
	}

	private List<GCData> loadCaches(String file) throws Exception{
		List<GCData> cachesList = new ArrayList<GCData>();
		List<String> readLines = IOUtils.readLines(new FileInputStream(new File(file)));
		for (String line : readLines) {
			String gcCode = line.trim();
			GCData data = new GCData(gcCode);
			
			HttpGet httpget = new HttpGet("http://www.geocaching.com/seek/cache_details.aspx?wp="+ gcCode);
	        HttpResponse response = httpclient.execute(httpget);
	        HttpEntity entity = response.getEntity();
	        String searchResult = EntityUtils.toString(entity);
	        Document cachePage = Jsoup.parse(searchResult);
	        data.setCoord(cachePage.select("#ctl00_ContentBody_LatLon").first().text());
	        data.setName(cachePage.select("#ctl00_ContentBody_CacheName").first().text());
	         
	        /* Schwierigkeit etc....
	        <span id="ctl00_ContentBody_uxLegendScale" title="(1 is easiest, 5 is hardest)"><img src="http://www.geocaching.com/images/stars/stars1.gif" alt="1 out of 5" /></span>
	        <span id="ctl00_ContentBody_Localize6" title="(1 is easiest, 5 is hardest)"><img src="http://www.geocaching.com/images/stars/stars1.gif" alt="1 out of 5" /></span>
	            Size:&nbsp;<span class="minorCacheDetails"><img src="/images/icons/container/micro.gif" alt="Size: Micro" title="Size: Micro" />&nbsp<small>(Micro)</small></span></p>
	        //ctl00_ContentBody_ShortDescription
	        //ctl00_ContentBody_LongDescription
	        */
	         
	        EntityUtils.consume(entity);
	        cachesList.add(data);
		}
		
		
		return cachesList;
	}
}
