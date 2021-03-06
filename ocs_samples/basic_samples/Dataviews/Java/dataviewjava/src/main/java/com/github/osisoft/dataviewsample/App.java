package com.github.osisoft.dataviewsample;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.time.*;

import  com.github.osisoft.ocs_sample_library_preview.*;
import  com.github.osisoft.ocs_sample_library_preview.sds.*;
import  com.github.osisoft.ocs_sample_library_preview.dataviews.*;

public class App {
    // get configuration
    static String tenantId = getConfiguration("tenantId");
    static String namespaceId = getConfiguration("namespaceId");
    static String ocsServerUrl = getConfiguration("ocsServerUrl");	
	
    // id strings
    static String sampleDataviewId = "Dataview_Sample";
    
    static String sampleDataviewName = "Dataview_Sample_Name"; 
    static String sampleDataviewDescription = "A Sample Description that describes that this Dataview is just used for our sample.";
    static String sampleDataviewDescription_modified = "A longer sample description that describes that this Dataview is just used for our sample and this part shows a put.";


    static String samplePressureTypeId = "Time_Pressure_SampleType";
    static String samplePressureStreamId = "Tank_Pressure_SampleStream";
    static String samplePressureStreamName = "Tank Pressure SampleStream";

    static String sampleTemperatureTypeId = "Time_Temperature_SampleType";
    static String sampleTemperatureStreamId = "Tank_Temperature_SampleStream";    
    static String sampleTemperatureStreamName = "Tank Temperature SampleStream";

    static boolean needData = true;

    
    public static void main(String[] args) throws InterruptedException {
    	
        toRun();
    }

    public static Boolean toRun() {
        Boolean success = true;
        // Create Sds client to communicate with server
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println(" ######                                                   #    #    #     #    #    ");
        System.out.println(" #     #   ##   #####   ##   #    # # ###### #    #       #   # #   #     #   # #   ");
        System.out.println(" #     #  #  #    #    #  #  #    # # #      #    #       #  #   #  #     #  #   #  ");
        System.out.println(" #     # #    #   #   #    # #    # # #####  #    #       # #     # #     # #     # ");
        System.out.println(" #     # ######   #   ###### #    # # #      # ## # #     # #######  #   #  ####### ");
        System.out.println(" #     # #    #   #   #    #  #  #  # #      ##  ## #     # #     #   # #   #     # ");
        System.out.println(" ######  #    #   #   #    #   ##   # ###### #    #  #####  #     #    #    #     # ");
        System.out.println("------------------------------------------------------------------------------------");

        OCSClient ocsClient = new OCSClient();
        
        try {

            if (needData) {
                createData(ocsClient);
            }
            String sampleStreamId = "SampleStream";
/*
            ######################################################################################################
            # Dataviews
            ######################################################################################################	
    
            #We need to create the dataview.  Dataview are complex objects.
            #For our dataview we are going to combine the two streams that were created, using a search to find the streams, using a common part of their name. 
            #We are using the default mappings.  This means our columns will keep their original names.  Another typical use of columns is to change what stream properties get mapped to which column.  
            #Mappings allow you to rename a column in the results to something different.  So if we want to we could rename Pressure to press.
            #We then define the IndexDataType.  Currently only datetime is supported.
            #Next we need to define the grouping rules.  Grouping decides how each row in the result is filled in. 
            #In this case we are grouping by tag, which effectively squashes are results together so that way Pressure and Temperature and Time all get results in a row.
            #If we grouped by StreamName, each row would be filled is as fully as it can by each Stream name.  Giving us results with Pressure and Time seperate from Pressure and Temperature
            #Our results when looking at it like a table looks like:
            #time,DefaultGroupRule_Tags,pressure,temperature
            #2019-02-18T18:50:17.1084594Z,(NoTags),13.8038967965309,57.6749982613741
            #2019-02-18T18:51:17.1084594Z,(NoTags),13.8038967965309,57.674998261374
            #....
            */

            DataviewQuery dataviewQuery = new DataviewQuery(sampleDataviewId, "streams", "name", sampleStreamId, "contains");
            DataviewGroupRule dataviewGroupRule = new DataviewGroupRule("DefaultGroupRule", "StreamTag");
            DataviewMapping dataviewMapping = new DataviewMapping();
            Dataview dataview = new Dataview(sampleDataviewId, sampleDataviewName, sampleDataviewDescription, 
                new DataviewQuery[] { dataviewQuery }, new DataviewGroupRule[] { dataviewGroupRule }, dataviewMapping,  null,  "datetime");

            System.out.println();
            System.out.println("Creating dataview");
            System.out.println(ocsClient.mGson.toJson(dataview));

            Dataview dataviewOut = ocsClient.Dataviews.postDataview(tenantId, namespaceId, dataview);
            dataview.setDescription(sampleDataviewDescription_modified);

            if(!(Objects.equals(dataviewOut.getId(),sampleDataviewId) && Objects.equals(dataviewOut.getDescription(),sampleDataviewDescription)))
            {
                throw new SdsError("Dataview doesn't match expected one");
            }
            
            dataviewOut = ocsClient.Dataviews.putDataview(tenantId, namespaceId, dataview);
            

            if(!(Objects.equals(dataviewOut.getId(),sampleDataviewId) && Objects.equals(dataviewOut.getDescription(),sampleDataviewDescription_modified)))
            {
                throw new SdsError("Dataview modified doesn't match expected one");
            }

            // Getting the complete set of dataviews to make sure it is there
            System.out.println();
            System.out.println("Getting dataviews");
            ArrayList<Dataview> dataviews = ocsClient.Dataviews.getDataviews(tenantId, namespaceId);
            for (Dataview dv : dataviews) {
                System.out.println(ocsClient.mGson.toJson(dv));
            }
            
            System.out.println();
            System.out.println("Getting datagroups");

            //This works in automated envrionment, but the below code works locally and gives you the datagroup back as an object
            String dataGroups = ocsClient.Dataviews.getDatagroupsString(tenantId, namespaceId, sampleDataviewId, 0, 100);
            System.out.println("Datagroups");
            System.out.println(dataGroups);

            //This should work to get datagroups and does locally
            /*
            Datagroups dataGroups = ocsClient.Dataviews.getDatagroups(tenantId, namespaceId, sampleDataviewId, 0, 100);
            for (Datagroup dg : dataGroups.getDataGroups().values()) {
                System.out.println("Datagroup");
                System.out.println(ocsClient.mGson.toJson(dg));
            }
            */
 
            ///By default the preview get interpolated values every minute over the last hour, which lines up with our data that we sent in.  
            ///Beyond the normal API optoins, this function does have the option to return the data in a class if you have created a Type for the data you are retreiving.

            System.out.println();
            System.out.println("Retrieving preview data from the Dataview");
            Map<String, Object>[] dataviewPreviewData =  ocsClient.jsonStringToMapArray(ocsClient.Dataviews.getDataviewPreview(tenantId, namespaceId,
                    sampleDataviewId));
           System.out.println(ocsClient.mGson.toJson(dataviewPreviewData[0]));


           //#Now we get the data creating a session.  The session allows us to get pages of data ensuring that the underlying data won't change as we collect the pages.
           //#There are apis to manage the sessions, but that is beyond the scope of this basic example.
           //#To highlight the use of the sessions this we will access the data, and wait 5 seconds to see the difference in the returned time.

           System.out.println();
           System.out.println("Retrieving session data from the Dataview");
           Map<String, Object>[] dataviewSessionData = ocsClient.jsonStringToMapArray(ocsClient.Dataviews.getDataviewInterpolated(tenantId, namespaceId,
                   sampleDataviewId, "","","","",0));
          System.out.println(ocsClient.mGson.toJson(dataviewSessionData[0]));

          
        
        
          System.out.println(("Intentional waiting for 5 seconds to show a noticeable change in time."));
          //# We wait for 5 seconds so the preview is different that before, but our session data should be the same
          TimeUnit.SECONDS.sleep(5);

          
          Map<String, Object>[] dataviewPreviewData2 = ocsClient.jsonStringToMapArray(ocsClient.Dataviews.getDataviewPreview(tenantId, namespaceId,
          sampleDataviewId));
          System.out.println(ocsClient.mGson.toJson(dataviewPreviewData2[0]));
          
          Map<String, Object>[] dataviewSessionData2 = ocsClient.jsonStringToMapArray(ocsClient.Dataviews.getDataviewInterpolated(tenantId, namespaceId,
          sampleDataviewId, "","","","",0));
          System.out.println(ocsClient.mGson.toJson(dataviewSessionData2[0]));

          
          if(!(Objects.equals(dataviewSessionData2[0],dataviewSessionData[0])))
          {
              throw new SdsError("Dataview session data doesn't match expected one");
          } 
          
          if(Objects.equals(dataviewPreviewData[0],dataviewPreviewData2[0]))
          {
              throw new SdsError("Dataview preview data matches");
          }
          
          
          System.out.println();
          System.out.println("Retrieving preview data from the Dataview in table format with headers");
          String dataviewSessionDataTable = ocsClient.Dataviews.getDataviewInterpolated(tenantId, namespaceId,
                  sampleDataviewId, "","","","csvh",0);
         System.out.println(dataviewSessionDataTable.substring(0,193));



        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            System.out.println("Cleaning up");
            if (needData) {
                cleanUp(ocsClient);
            }
            try {
                ocsClient.Dataviews.deleteDataview(tenantId, namespaceId, sampleDataviewId);
            } catch (Exception e) {
                e.printStackTrace();                
                success = false;
            }
        }
        return success;
    }
    
    private static void createData(OCSClient ocsClient) throws Exception {
        
        try {

            SdsType doubleType = new SdsType("doubleType","", "", SdsTypeCode.Double);
            SdsType dateTimeType = new SdsType("dateTimeType","","", SdsTypeCode.DateTime);

            SdsTypeProperty pressureDoubleProperty = new SdsTypeProperty("pressure","","",doubleType, false);
            SdsTypeProperty temperatureDoubleProperty = new SdsTypeProperty("temperature", "", "", doubleType, false);            
            SdsTypeProperty timeDateTimeProperty = new SdsTypeProperty("time", "","", dateTimeType, true);

            // Create a SdsType for our WaveData class; the metadata properties are the ones we just created
            
            SdsType pressure_SDSType = new SdsType(samplePressureTypeId, "","", SdsTypeCode.Object, new SdsTypeProperty[] {pressureDoubleProperty, timeDateTimeProperty}) ;
            SdsType temperature_SDSType = new SdsType(sampleTemperatureTypeId, "","", SdsTypeCode.Object, new SdsTypeProperty[] {temperatureDoubleProperty, timeDateTimeProperty}) ;            


            System.out.println("Creating SDS Type");

            ocsClient.Types.createType(tenantId, namespaceId, pressure_SDSType);
            ocsClient.Types.createType(tenantId, namespaceId, temperature_SDSType);

            SdsStream pressureStream = new SdsStream(samplePressureStreamId, samplePressureTypeId, "",samplePressureStreamName);
            SdsStream temperatureStream = new SdsStream(sampleTemperatureStreamId, sampleTemperatureTypeId, "", sampleTemperatureStreamName);
            
            System.out.println("Creating SDS Streams");
            String jsonStream = ocsClient.Streams.createStream(tenantId, namespaceId, pressureStream);
            jsonStream = ocsClient.Streams.createStream(tenantId, namespaceId, temperatureStream);

            Instant start = Instant.now().minus(Duration.ofHours(1));

            ArrayList<String> pressureValues = new ArrayList<String>();
            ArrayList<String> temperatureValues = new ArrayList<String>();

            System.out.println("Creating values");
            for (int i = 1; i < 60; i += 1) 
            {
                String pVal =  ("{\"time\" : \""+ start.plus(Duration.ofMinutes(i* 1)) +"\", \"pressure\":" + Math.random() * 100 + "}");
                String tVal =  ("{\"time\" : \""+ start.plus(Duration.ofMinutes(i* 1)) +"\", \"temperature\":" + (Math.random() * 20 + 50) + "}");
                pressureValues.add(pVal);
                temperatureValues.add(tVal);
            }

            String pVals = "[" + String.join(",", pressureValues) + "]";
            String tVals = "[" + String.join(",", temperatureValues) + "]";

            System.out.println("Sending pressure values");
            ocsClient.Streams.updateValues(tenantId, namespaceId, samplePressureStreamId, pVals);
            System.out.println("Sending temperature values");
            ocsClient.Streams.updateValues(tenantId, namespaceId, sampleTemperatureStreamId, tVals);

        }
        catch (Exception e) {
            printError("Error creating Sds Objects", e);
            throw e;
        }
    }

    /**
     * Prints out a formated error string
     *
     * @param exceptionDescription - the description of what the error is
     * @param exception            - the exception thrown
     */
    private static void printError(String exceptionDescription, Exception exception) {
        System.out.println("\n\n======= " + exceptionDescription + " =======");
        System.out.println(exception.toString());
        System.out.println("======= End of " + exceptionDescription + " =======");
    }

    
    private static String getConfiguration(String propertyId) {
        
        String property = "";
        Properties props = new Properties();
        InputStream inputStream;

        try {
            inputStream = new FileInputStream("config.properties"); 
            //if launching from git folder use this:
            // "\\basic_samples\\Dataviews\\JAVA\\config.properties");
            props.load(inputStream);
            property = props.getProperty(propertyId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return property;
    }
	
    public static void cleanUp(OCSClient ocsClient) 
	{
        System.out.println("Deleting the streams");
        try{ ocsClient.Streams.deleteStream(tenantId, namespaceId, samplePressureStreamId);}catch(Exception e) {e.printStackTrace();}
        try{ocsClient.Streams.deleteStream(tenantId, namespaceId, sampleTemperatureStreamId);}catch(Exception e) {e.printStackTrace();}

        System.out.println("Deleting the types");
        try{ocsClient.Types.deleteType(tenantId, namespaceId, samplePressureTypeId);}catch(Exception e) {e.printStackTrace();}
        try{ocsClient.Types.deleteType(tenantId, namespaceId, sampleTemperatureTypeId);}catch(Exception e) {e.printStackTrace();}
    }
}
