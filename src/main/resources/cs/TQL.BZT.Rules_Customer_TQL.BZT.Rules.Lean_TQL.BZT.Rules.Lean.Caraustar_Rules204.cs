using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using TQL.BZT.BLL.DataContracts;
using TQL.BZT.Rules.Utils;


namespace TQL.BZT.Rules.Lean.Caraustar
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
                /*********  If the original EDI is required in case information was not mapped.
                 * call this method
                 * 
                    string CurrentEdi204 = CommonX12.GetEDI204Transaction(edi204Obj);

                */

                /**************************************************************
                 *      Populate Group Values
                 *************************************************************/

                //Populate Customer PO. TQL Default pulls from B204 Shipment ID
                string customerpo = "your logic here";
                edi204Obj.LOADHDR.LOADHDRINFO.CUSTOMERPO = customerpo;


                //Populate Pickup # 
                /* Sample Code to populate L11 with BM qualifier of pick
                 * 
                 * Replace with Custom Code
                 * */
                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Pick))  //Loop through picks
                {


                    string pickupnumber = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "L11", "BM", ",", "", 0);
                    stop.PICKUPDELIVERYNO = pickupnumber;

                }

                //Populate Delivery PO# 
                /* Sample Code to populate L11 with PO qualifier of drop
                 * 
                 * Replace with Custom Code
                 * */
                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Drop))  //Loop through drops
                {

                    string deliverypo = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "L11", "PO", ",", "", 0);
                    stop.PICKUPDELIVERYNO = "";
                }

                /********************************************
                 * Any additional Custom Group Logic Goes Here
                 * ******************************************
                 * .
                 * *****************************************/

                /***********************************************************
                 * Call Custom Logic Here For this Group.
                 * Need to create separate project for each Customer.
                 * Need to reference that project in the group project
                ************************************************************/


            }
            catch (Exception ex)
            {
                TQL.BZT.Logger.LogGenerator.Log(true, false, true, true, false, EventLogEntryType.Error, ex);
                throw ex;
            }





        }


        /**************************************************************************
         * Custstomer Values. This section not used in Group Projects.
         * ***********************************************************************/

        /// <summary>
        /// Custom Settings for individual customer
        /// </summary>
        /// <param name="edi204Obj">ref 204 DataContract Object</param>
        /// <returns>204 DataContract Object by Reference</returns>
        public static void setCustomerValues(ref TQLBZTEDI204 edi204Obj)
        {

            //add customer settings code here.
            try
            {

                /**************************************************************
                 *      Populate Customer Values
                 *************************************************************/

                //Populate Customer PO. TQL Default pulls from B204 Shipment ID
                //string customerpo = "your logic here";

                string pickupnumber = "";
                string deliverypo = "";


                foreach (RefNumberInfo refnum in edi204Obj.LOADHDR.REFNUMBERINFOCollection.Items)
                {
                    //Set PU Number to L11/WH Qualifier;
                    if (refnum.REFNUMBERQUAL == "WH")
                    {
                        pickupnumber = refnum.REFNUMBER;
                    }

                    //Set Delivery PO to L11/CO Qualifier;
                    if (refnum.REFNUMBERQUAL == "CO")
                    {
                        deliverypo = refnum.REFNUMBER;
                    }
                }

                //Populate Pickup # 
                /* Sample Code to populate L11 with BM qualifier of pick
                 * 
                 * Replace with Custom Code
                 * */
                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Pick))  //Loop through picks
                {
                    //string pickupnumber = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "OID01", "OID01", ",", "", 0);
                    stop.PICKUPDELIVERYNO = pickupnumber;

                }

                //Populate Delivery PO# 
                /* Sample Code to populate L11 with PO qualifier of drop
                 * 
                 * Replace with Custom Code
                 * */
                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Drop))  //Loop through drops
                {
                    //string deliverypo = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "OID01", "OID01", ",", "", 0);
                    stop.PICKUPDELIVERYNO = deliverypo;
                }

                /********************************************
                 * Any additional Customer Logic Goes Here
                 * ******************************************
                 * .
                 * *****************************************/
            }
            catch (Exception ex)
            {
                TQL.BZT.Logger.LogGenerator.Log(true, false, true, true, false, EventLogEntryType.Error, ex);
                throw ex;
            }



        }


    }
}
