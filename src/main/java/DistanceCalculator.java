import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Scanner;
import java.text.DecimalFormat;
public class DistanceCalculator {

	//Converting Degree:Minute:Second coordinates to decimal degrees
	static double dmsTodd (double d, double m, double s, String dir)
	{
		double multiplier = 1.0;
		//If south or west, decimal degrees are negative
		if (dir.equals("S") || dir.equals("W"))
			multiplier = -1.0;
		
		return  (d + m /60 + s /3600) * multiplier;
	}

    //Haversine formula, used to convert distance between two dd points, taken from Rosetta Code
    static double haversine (double lat1,double lon1,double lat2,double lon2)
	{
        //Radius of earth in km
        final double  R = 6372.8;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
	}

    static Document getCityPage(String city, Scanner sc) throws Exception {
        String relHref, choice;
        Document doc;
        Elements places, check;
        //Attempt connecting to the corresponding Wikipedia page
        doc = Jsoup.connect("https://en.wikipedia.org/wiki/" + city.replace(' ', '_')).userAgent("Mozilla/5.0").timeout(0).get();
        //Checking to see if "city" page is a disambiguation page (e.g. wikipedia.org/wiki/Markham)
        check = doc.select(".mw-normal-catlinks li");
        //if page falls under disambiguation category
        if (check.text().contains("disambiguation")) {
            //Find listed items that are cities
            places = doc.select("li:contains(a city)");
            if (places.size() > 0) {
                //Output possible cities user may be referring to
                System.out.println("Ambiguous choice. Select the number corresponding to city you are referring to:");
                for (int i = 0; i < places.size(); i++) {
                    System.out.println("[" + i + "]" + " " + places.get(i).text());
                }
                choice = sc.nextLine();
                //If input is valid
                if (choice.length() == 1 && Character.isDigit(choice.charAt(0)) && Integer.parseInt(choice) >= 0 && Integer.parseInt(choice) < places.size()) {
                    //Extract relative link from li's child node
                    relHref = places.get(Integer.parseInt(choice)).child(0).attr("href");
                    //Connect to desired city page
                    doc = Jsoup.connect("https://en.wikipedia.org" + relHref).userAgent("Mozilla/5.0").timeout(0).get();
                }
            }
            //if input is invalid, end the program. TODO: modify this method to loop until input is valid
            else {
                System.out.println("Invalid choice");
                sc.close();
                throw new Exception();
            }
        }
        return doc;
    }

    public static void main (String args[])
    {
        String city1, city2;
        //best naming convention :')
        //dms and dd coordinates for cities
        String lat1[];
        String lon1[];
        String lat2[];
        String lon2[];
        double decDegrees1[] = new double[2];
        double decDegrees2[] = new double[2];
        Scanner sc = new Scanner(System.in);
        //For formatting numbers
        DecimalFormat df = new DecimalFormat("#.##");
        Document doc;
        Elements el;
        //Getting user cities and Wikipedia pages
        try {

            System.out.println("Welcome to Distance Calculator.\n\nEnter city 1");
            city1 = sc.nextLine();
            doc = getCityPage(city1, sc);
            //Try to locate the coordinates on the page
            el = doc.select(".geo-dms span");
        lat1 = el.get(0).text().split("\\W+");
        lon1 = el.get(1).text().split("\\W+");
            System.out.println("Enter city 2");
            city2 = sc.nextLine();
            doc = getCityPage(city2, sc);
        el = doc.select(".geo-dms span");        	
        lat2 = el.get(0).text().split("\\W+");
        lon2 = el.get(1).text().split("\\W+");
        // if dms only contains dm (e.g. 46 42N vs. 46 42 30N)
        if (lat1.length == 3)
        {
        	decDegrees1[0] = dmsTodd(Double.parseDouble(lat1[0]),Double.parseDouble(lat1[1]),0,lat1[2]);
        	decDegrees1[1] = dmsTodd(Double.parseDouble(lon1[0]),Double.parseDouble(lon1[1]),0,lon1[2]);
        }
        else
        {
        	decDegrees1[0] = dmsTodd(Double.parseDouble(lat1[0]),Double.parseDouble(lat1[1]),Double.parseDouble(lat1[2]),lat1[3]);
        	decDegrees1[1] = dmsTodd(Double.parseDouble(lon1[0]),Double.parseDouble(lon1[1]),Double.parseDouble(lon1[2]),lon1[3]);       	
        }
        if (lat2.length == 3)
        {
        	decDegrees2[0] = dmsTodd(Double.parseDouble(lat2[0]),Double.parseDouble(lat2[1]),0,lat2[2]);
        	decDegrees2[1] = dmsTodd(Double.parseDouble(lon2[0]),Double.parseDouble(lon2[1]),0,lon2[2]);
        }
        else
        {
        	decDegrees2[0] = dmsTodd(Double.parseDouble(lat2[0]),Double.parseDouble(lat2[1]),Double.parseDouble(lat2[2]),lat2[3]);
        	decDegrees2[1] = dmsTodd(Double.parseDouble(lon2[0]),Double.parseDouble(lon2[1]),Double.parseDouble(lon2[2]),lon2[3]);       	
        }
        //
            System.out.println(city1 + " coordinates: " + df.format(decDegrees1[0]) + ", " + df.format(decDegrees1[1]) + "\n" + city2 + " coordinates: " + df.format(decDegrees2[0]) + ", " + df.format(decDegrees2[1]));
        System.out.print("Distance between " + city1 + " and " + city2 + " is: " + df.format(haversine(decDegrees1[0],decDegrees1[1],decDegrees2[0],decDegrees2[1])) + " km.");
        }
        catch (Exception e) 
        {

            System.out.println("Error. Unable to find city.");
            e.printStackTrace();
        }
        sc.close();
    }
}
