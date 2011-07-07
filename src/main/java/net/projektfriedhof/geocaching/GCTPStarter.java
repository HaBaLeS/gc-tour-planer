package net.projektfriedhof.geocaching;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
	}

	private void buildKMLFromList(List<GCData> caches) {
		for (GCData gcData : caches) {
			System.out.println(gcData.getCoord() + "\t" +gcData.getName() );
		}
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
