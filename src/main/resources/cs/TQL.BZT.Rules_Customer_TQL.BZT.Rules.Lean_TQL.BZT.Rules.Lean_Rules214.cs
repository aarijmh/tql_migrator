using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.IO;
using TQL.BZT.BLL.DataContracts;
using TQL.BZT.Rules.Utils;

namespace TQL.BZT.Rules.Lean
{
    public class Rules214
    {
        /// <summary>
        /// Common 214 Settings for the group.
        /// ExtraCode to be added.But code not to be removed from Group settings.
        /// </summary>
        /// <param name="edi214Obj">ref 214 DataContract Object</param>
        /// <returns>214 DataContract Object by Reference</returns>
        public static void setGroupValues(ref TQLBZTEDI214 edi214Obj)
        {
            #region Rules
            /****       Initialize RulesInfoCollection for the Group  *****/
            RulesInfoCollection rulesinfocollection = new RulesInfoCollection();
            rulesinfocollection = edi214Obj.LOADHDR.RULEINFOCollection;

            /******    Set include/exclude flag. RuleType = 8   ******/
            /******    Derived from the defaults then modified  ******/

            //These are the most common used. Include them all. Let developer turn them off.
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "N1Loop1", "T");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "N1Loop1_N1", "T");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "N1Loop1_N3", "T");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "N1Loop1_N4", "T");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "N1Loop1_G62", "F");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1", "T");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS1", "F");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS2", "F");

            //10/2012 - Now there is a requirement to pass longitute, latitude. Will be pulled from tblZipCodes (spPopulateLongLatbyCityState)
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS101", "F");  //99.99999% of maps
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS104", "F");

            //Need to set the MS203 value to TL by default. At least one customer requires a different value.
            Common.UpdateHardCodedValueRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS203", "TL");

            //Controlled by group and customer rules.
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "L11", "T");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "MAN", "F");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "K1", "F");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "N1Loop1_N2", "F");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "N1Loop1_L11_2", "F");
            Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_L11_3", "F");
            #endregion

            #region Stops/Events
            foreach (StopInfo214 stop in edi214Obj.STOPINFOCollection.Items)
            {

                //userStory 177178 Address Not Provided - 12/1/21
                if (String.IsNullOrEmpty(stop.ADDRESSINFO.ADDRESS))
                {
                    stop.ADDRESSINFO.ADDRESS = "Address Not Provided";
                }

                //Loop through stops and set first and last stop flags to false
                stop.ISFIRSTSTOP = false;
                stop.ISLASTSTOP = false;

                if (stop.EventInfoCollection != null)
                {
                    foreach (EventInfo eventinfo in stop.EventInfoCollection.Items)
                    {
                        int? eventid = eventinfo.EVENTID;
                        int? recordstatus = eventinfo.RECORDSTATUS;
                        if (eventid > 0) //Event being processed because the event node will only be added to the stop being processed.
                        {
                            if (eventinfo.EVENTSTATUS == "X6")
                            {
                                Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS1", "T");
                                Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS101", "T");  //99.99999% of maps
                                Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "LXLoop1_AT7Loop1_MS104", "T");

                                #region Add in Lattitude/Longitude Longitude
                                //This requires a map change as well.

                                //First call the function; Typically done from within the event collection
                                UpdateLonLatInfo(ref edi214Obj);

                                //Will Use the "UpdateLonLatInfo and "FormatLonLatInfo" functions below.

                                #endregion
                            }

                            foreach (RefNumberInfo refNumber in stop.REFNUMBERINFOCollection.Items)
                            {
                                if (refNumber.REFNUMBERQUAL == "OID01")
                                {
                                    RefNumberInfo hrefnumber = new RefNumberInfo();
                                    hrefnumber.EDI214DESTINATION = "L11";
                                    hrefnumber.REFNUMBERQUAL = "OQ";
                                    hrefnumber.REFNUMBER = refNumber.REFNUMBER;
                                    edi214Obj.LOADHDR.REFNUMBERINFOCollection.Items.Add(hrefnumber);
                                }
                                else
                                {
                                    refNumber.EDI214DESTINATION = "";
                                }
                            }

                            if (!String.IsNullOrEmpty(eventinfo.ORDERNUMBER))
                            {
                                RefNumberInfo refnumber = new RefNumberInfo();
                                refnumber.EDI214DESTINATION = "L11";
                                refnumber.REFNUMBERQUAL = "AO";
                                refnumber.REFNUMBER = eventinfo.ORDERNUMBER.Trim();
                                edi214Obj.LOADHDR.REFNUMBERINFOCollection.Items.Add(refnumber);                            
                            }

                            //Set IsLastStop equal to true on the stop being reported
                            stop.ISLASTSTOP = true;
                        }
                    }
                }
            }
            #endregion

            #region Header Address to Stops
            //Add LoadHdr Address with "SH" qualifier to Stops; Set IsFirstStop to true;
            foreach (AddressInfo hdraddress in edi214Obj.LOADHDR.ADDRESSINFOCollection.Items)
            {
                if (hdraddress.ADDRESSQUAL == "SH")
                { 
                    StopInfo214 AddressHdr = new StopInfo214();
                    AddressHdr.PONUMBER = hdraddress.PONUMBER;
                    AddressHdr.BZTSTOPID = hdraddress.BZTSTOPID;
                    AddressHdr.ISFIRSTSTOP = true;
                    AddressInfo AddressInfoColl = new AddressInfo();
                    AddressInfoColl.PONUMBER = hdraddress.PONUMBER;
                    AddressInfoColl.BZTSTOPID = hdraddress.BZTSTOPID;
                    AddressInfoColl.HDRDETFLAG = hdraddress.HDRDETFLAG;
                    AddressInfoColl.TQLADDRESSID = hdraddress.ID;
                    AddressInfoColl.ADDRESSQUAL = hdraddress.ADDRESSQUAL;
                    AddressInfoColl.NAME = hdraddress.NAME;
                    AddressInfoColl.IDQUAL = hdraddress.IDQUAL;
                    AddressInfoColl.ID = hdraddress.ID;
                    AddressInfoColl.ADDRESS = hdraddress.ADDRESS;
                    AddressInfoColl.CITY = hdraddress.CITY;
                    AddressInfoColl.STATE = hdraddress.STATE;
                    AddressInfoColl.ZIP = hdraddress.ZIP;
                    AddressInfoColl.COUNTRY = hdraddress.COUNTRY;
                    AddressHdr.ADDRESSINFO = AddressInfoColl;
                    edi214Obj.STOPINFOCollection.Items.Insert(0, AddressHdr);
                }
            }
            #endregion

        }


        /// <summary>
        /// Custom Settings for individual customer
        /// </summary>
        /// <param name="edi214Obj">ref 214 DataContract Object</param>
        /// <returns>214 DataContract Object by Reference</returns>
        public static void setCustomerValues(ref TQLBZTEDI214 edi214Obj)
        {

        }

        //Pull Longitude and Latitude for MS1 segment;
        public static void UpdateLonLatInfo(ref TQLBZTEDI214 edi214obj)
        {
            foreach (StopInfo214 stop in Lists.GetSTOPSList(edi214obj))
            {
                if (stop.EventInfoCollection == null)
                    continue;

                foreach (EventInfo edievent in stop.EventInfoCollection.Items)
                {
                    double? latt = 0;
                    double? longt = 0;
                    TQL.BZT.Repositories.LMDataDTO.PopulateLongLatByCityState(edievent.EVENTCITY, edievent.EVENTSTATE, edievent.EVENTZIP, ref latt, ref longt);


                    if (longt == 0 || latt == 0) // City/State not matching zip in tblZipCodes table
                    {
                        TQL.BZT.Repositories.LMDataDTO.PopulateLongLatByCityState(edievent.EVENTCITY, edievent.EVENTSTATE, "", ref latt, ref longt);
                    }

                    //Take the absolute value of the longitude and latitude. Mainly to make Longitude positive.
                    decimal? longt2 = Math.Abs(Convert.ToDecimal(longt));
                    string sLongt = longt2.ToString();
                    decimal? latt2 = Math.Abs(Convert.ToDecimal(latt));
                    string sLatt = latt2.ToString();

                    sLongt = FormatLonLatInfo(sLongt);
                    sLatt = FormatLonLatInfo(sLatt);

                    edievent.EVENTDESC = sLongt;
                    edievent.EVENTZIP = sLatt;

                }
            }
        }

        //Format Longitude and Latitude for MS1 segment;
        public static string FormatLonLatInfo(string position)
        {
            if (position.Contains('.'))
            {
                /*  
                ***Short-hand: Hours must be 0-180, minutes must be 0-59, and seconds must be 0-59****  
                 
                As we found out with the new standards for the long/lat:
                • Longitude identifies East – West, with 0 being the Greenwich Meridian. There are three equivalent ways to express longitude:
                1. 0-180 East and 0-180 West, which is what the EDI specifications are currently set for
                2. 0-360 East, or just 0-360. In that case, 270 East is equivalent to 90 West. 
                3. -180 to +180, in this case -90 is equivalent to 90 West


                • Latitude identifies the North – South. There are three ways to express latitude:
                1. 0-90 North and 0-90 South, which is what the EDI specifications are currently set for
                2. -90 to +90, where -45 equivalent to 45 South.
                3. Colatitude, which is 0 at the North Pole, 90 at the equator, and 180 at the South Pole. So, 45 South is equivalent to colatitude of 135

                So it's the *longitude* not latitude. the seconds they are using is above 59.. They are using 77: 08212-77
                Should look like: 0821259, just anything below 59 for minutes and seconds will work. 
                 
                */

                //Take GPS coordinates, before decimal needs to be 3 positions and after needs to be 4. Data isn't accurate.
                string[] xinfo = position.Split('.');

                xinfo[0] = xinfo[0].PadLeft(3, '0');
                xinfo[1] = xinfo[1].PadRight(10, '0').Substring(0, 4);

                string sHours = xinfo[0];
                string sMinSec = xinfo[1];
                string sMinutes = sMinSec.Substring(0, 2);
                string sSeconds = sMinSec.Substring(2, 2);

                int iHours = Convert.ToInt32(sHours);
                int iMinutes = Convert.ToInt32(sMinutes);
                int iSeconds = Convert.ToInt32(sSeconds);

                if (iHours > 180) iHours = 180;
                sHours = iHours.ToString().PadLeft(3, '0');

                if (iMinutes > 59) iMinutes = 59;
                sMinutes = iMinutes.ToString().PadLeft(2, '0');

                if (iSeconds > 59) iSeconds = 59;
                sSeconds = iSeconds.ToString().PadLeft(2, '0');


                position = sHours + sMinutes + sSeconds;

                return position;
            }
            else return position;

        }


    }
}
