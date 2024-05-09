using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using TQL.BZT.BLL.DataContracts;
using TQL.BZT.Rules.Utils;


namespace TQL.BZT.Rules.Lean
{
    public class Rules204
    {
        /// <summary>
        /// Common 204 Settings for the group.
        /// ExtraCode to be added.But code not to be removed from Group settings.
        /// </summary>
        /// <param name="edi204Obj">ref 204 DataContract Object</param>
        /// <returns>204 DataContract Object by Reference</returns>
        public static void setGroupValues(ref TQLBZTEDI204 edi204Obj)
        {

            //add group settings code here.
            try
            {

                #region User Story 277684 Overwite Shed ID to resolve auto accept shed issues
                int? CID = edi204Obj.TPPROFILEINFO.CUSTOMERID;
                //195835 = Husqvarna 4/19/2023
                //1909940 = Sealed Air Corp added 4/28/2023
                if (CID == 195835 || CID == 1909940)
                {
                    foreach (StopInfo204 stop in edi204Obj.STOPS.Items)
                    {
                        //Want to overwrite whatever the customer is sending us to make sure we are using unique values
                        string addID = "";
                        string addNumber = "";
                        string zipCode = "";
                        string[] addressLineArray = null;
                        zipCode = stop.ADDRESSINFO.ZIP;
                        addressLineArray = stop.ADDRESSINFO.ADDRESS.Split(' ');
                        addNumber = addressLineArray[0];
                        addID = String.Format("{0}_{1}", addNumber, zipCode);
                        stop.ADDRESSINFO.ID = addID;
                        stop.ADDRESSINFO.IDQUAL = "ZZ";
                    }
                }
                #endregion

                #region Address Not Provided at Header Level
                foreach (AddressInfo hdraddress in edi204Obj.LOADHDR.ADDRESSINFOCollection.Items)
                {
                    if (String.IsNullOrEmpty(hdraddress.ADDRESS))
                    {                       
                        hdraddress.ADDRESS = "Address Not Provided";
                    }
                }
                #endregion  

                /**************************************************************
                 *      Populate Group Values
                 *************************************************************/

                //Lean is sending the L303 rate with no decimal. Need to set to FrtRate = Charge which has decimal.
                edi204Obj.LOADHDR.LOADHDRINFO.FRTRATE = edi204Obj.LOADHDR.LOADHDRINFO.CHARGE;

                //foreach (StopInfo204 stop in edi204Obj.STOPS.Items)
                //{
                //    if (String.IsNullOrEmpty(stop.ADDRESSINFO.ADDRESSQUAL))
                //    {
                //        stop.ADDRESSINFO.ADDRESSQUAL = "ZZ";                                         
                //    }
                    
                //    else if (stop.ADDRESSINFO.ADDRESSQUAL.Length < 2)
                //    {
                //        stop.ADDRESSINFO.ADDRESSQUAL = stop.ADDRESSINFO.ADDRESSQUAL.PadLeft(2,'0');
                //    }
                //}



                /*
                                //Populate Customer PO. TQL Default pulls from B204 Shipment ID
                                string customerpo = String.Empty ; //B204
                                //s501=1 , OID01
                                if (edi204Obj.STOPS.Items[0].ISFIRSTSTOP)
                                {
                                    RefNumberInfoCollection refColl = edi204Obj.STOPS.Items[0].REFNUMBERINFOCollection;
                                    if (refColl.Items[0].EDISOURCE == "OID01")
                                    {
                                        customerpo = String.IsNullOrEmpty(customerpo) ? (refColl.Items[0].REFNUMBER) : customerpo;
                                    }
                                }
                                edi204Obj.LOADHDR.LOADHDRINFO.CUSTOMERPO = customerpo;


                                //Populate Pickup # 
                                /* Sample Code to populate L11 with BM qualifier of pick
                                 * 
                                 * Replace with Custom Code
                                 * */
                /*                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Pick))  //Loop through picks
                                {
                                    string pickupnumber = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "OID01", "OID01", ",", "", 0);
                                    stop.PICKUPDELIVERYNO = pickupnumber;

                                }

                                //Populate Delivery PO# 
                                /* Sample Code to populate L11 with PO qualifier of drop
                                 * 
                                 * Replace with Custom Code
                                 * */
                /*                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Drop))  //Loop through picks
                                {
                                    string deliverypo = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "OID01", "OID01", ",", "", 0);
                                    stop.PICKUPDELIVERYNO = deliverypo;

                                }
                 */
                /********************************************
                 * Any additional Custom Group Logic Goes Here
                 * ******************************************
                 * .
                 * *****************************************/

                /***********************************************************
                 * Call Custom Logig Here For this Group.
                 * Need to create separate project for each Customer.
                 * Need to reference that project in the group project
                ************************************************************/

                // int tpprofileid = edi204Obj.



                int? tpprofileid = edi204Obj.EDIPROPERTIES.TPPROFILEID;


                /*
                switch (tpprofileid)
                {


                    //case 51:
                    //    LeprinoFoods.Rules204.setCustomerValues(ref edi204Obj);
                    //    break;

                    case 245:
                        ChepPallet.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 246:
                        ChepCanada.Rules204.setCustomerValues(ref edi204Obj);
                        break;
                
                    case 289:
                        DreyersGrandIce.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 291:
                        OTISSpunkmeyer.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 313:
                        Ralcorp.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 357:
                        NorthwestPipe.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 413:
                        Keystone.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 51:
                        Leprino.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 295:
                        PierreFoods.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 370:
                        MapleHurst.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 433:
                        MainesPaper.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 477:
                        NicholasCo.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    case 486:
                        ConcoFoodService.Rules204.setCustomerValues(ref edi204Obj);
                        break;

                    //case 280:
                    //    GreatKitchens.Rules204.setCustomerValues(ref edi204Obj);
                    //    break;

                    //case 301:
                    //    Dannon.Rules204.setCustomerValues(ref edi204Obj);
                    //    break;

                    //case 280:
                    //    GreatKitchens.Rules204.setCustomerValues(ref edi204Obj);
                    //    break;
                }
                */

                //userStory 176743
                foreach (StopInfo204 stop in edi204Obj.STOPS.Items)
                {
                    if(stop.ADDRESSINFO.CITY == "RIVERSIDE" && stop.ADDRESSINFO.ZIP == "92509")
                    {
                        stop.ADDRESSINFO.CITY = "JURUPA VALLEY";
                    }
                }

                bool isQTUFound = false;
                String quoteNote = "";
                foreach (NoteInfo note in edi204Obj.LOADHDR.NOTEINFOCollection.Items)
                {
                    if (note.NOTEQUAL.ToUpper().Contains("QUT"))
                    {
                        quoteNote = note.NOTE;
                        isQTUFound = true;
                    }
                }

                if (isQTUFound == true)
                {
                    NoteInfo quote = new NoteInfo();
                    quote.NOTEQUAL = "ZZZ";
                    quote.NOTE = "Quote ID: " + quoteNote;
                    quote.HDRDETFLAG = "H";
                    edi204Obj.LOADHDR.NOTEINFOCollection.Items.Add(quote);

                    NoteInfo DPNote = new NoteInfo();
                    DPNote.NOTEQUAL = "ZZZ";
                    DPNote.NOTE = "Awarded from Dynamic Pricing - Load Must be Accepted";
                    DPNote.HDRDETFLAG = "H";
                    edi204Obj.LOADHDR.NOTEINFOCollection.Items.Add(DPNote);

                    RefNumberInfo refnum = new RefNumberInfo();
                    refnum.BZTSTOPID = 0;
                    refnum.EDISOURCE = "L1101";
                    refnum.HDRDETFLAG = "H";
                    refnum.REFNUMBER = quoteNote;
                    refnum.REFNUMBERQUAL = "Q1";
                    edi204Obj.LOADHDR.REFNUMBERINFOCollection.Items.Add(refnum);
                }
            }
            catch (Exception ex)
            {
                TQL.BZT.Logger.LogGenerator.Log(true, false, true, true, false, EventLogEntryType.Error, ex);
                throw ex;
            }





        }


    }
}
