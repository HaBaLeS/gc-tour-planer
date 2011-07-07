package net.projektfriedhof.geocaching;

import java.util.ArrayList;
import java.util.List;

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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GCTPStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new GCTPStarter().run();
	}

	public void run(){
		
		
		try {
			testLogin();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
	}

	private void testLogin() throws Exception{
		
		 DefaultHttpClient httpclient = new DefaultHttpClient();
		
		final String host = "http://www.geocaching.com";
		final String path = "/login/default.aspx";
		
		
		 HttpGet httpget = new HttpGet(host+path);

         HttpResponse response = httpclient.execute(httpget);
         HttpEntity entity = response.getEntity();

         System.out.println("Login form get: " + response.getStatusLine());
         String loginPageString = EntityUtils.toString(entity);
         EntityUtils.consume(entity);
         
         Document loginPage = Jsoup.parse(loginPageString);
         Elements inputFields = loginPage.select("input");
         
         for (Element element : inputFields) {
        	 System.out.println(element);
		}
         
         //Find viewState
         String viewstate = "";
         String viewstate1 = null;
        
         //BuildLogin
         
         
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
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$tbUsername", "USR"));
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$tbPassword", "PASS"));
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$cbRememberMe", "on"));
 		nvps.add(new BasicNameValuePair("ctl00$SiteContent$btnSignIn", "Login"));
         
         httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

         response = httpclient.execute(httpost);
         entity = response.getEntity();

         System.out.println("Login form get: " + response.getStatusLine());
         String loginAnswer = EntityUtils.toString(entity);
      //   System.out.println(loginAnswer);
         EntityUtils.consume(entity);
         
         
//         List<Cookie> cookies = httpclient.getCookieStore().getCookies();
//         if (cookies.isEmpty()) {
//             System.out.println("None");
//         } else {
//             for (int i = 0; i < cookies.size(); i++) {
//                 System.out.println("- " + cookies.get(i).toString());
//             }
//         }
         
         
         //https://raw.github.com/carnero/c-geo/master/src/carnero/cgeo/cgBase.java
         
         
         //Get Page
         httpget = new HttpGet("http://www.geocaching.com/seek/cache_details.aspx?wp=GC29V8K");

         response = httpclient.execute(httpget);
         entity = response.getEntity();

         System.out.println("Login form get: " + response.getStatusLine());
         String searchResult = EntityUtils.toString(entity);
         
         //System.out.println(searchResult);
         Document cachePage = Jsoup.parse(searchResult);
         String coord = cachePage.select("#ctl00_ContentBody_LatLon").first().text();
         String name = cachePage.select("#ctl00_ContentBody_CacheName").first().text();
         
         /* Schwierigkeit etc....
         <span id="ctl00_ContentBody_uxLegendScale" title="(1 is easiest, 5 is hardest)"><img src="http://www.geocaching.com/images/stars/stars1.gif" alt="1 out of 5" /></span>

         <span id="ctl00_ContentBody_Localize6" title="(1 is easiest, 5 is hardest)"><img src="http://www.geocaching.com/images/stars/stars1.gif" alt="1 out of 5" /></span>
         
     </div>
     
     <div class="CacheSize span-9">
         
         <p style="text-align: center;">
             Size:&nbsp;<span class="minorCacheDetails"><img src="/images/icons/container/micro.gif" alt="Size: Micro" title="Size: Micro" />&nbsp<small>(Micro)</small></span></p>
         */
         
         //ctl00_ContentBody_ShortDescription
         //ctl00_ContentBody_LongDescription
         
         
         System.out.println(name + "" + coord);
         EntityUtils.consume(entity);
         
         
	}
}
