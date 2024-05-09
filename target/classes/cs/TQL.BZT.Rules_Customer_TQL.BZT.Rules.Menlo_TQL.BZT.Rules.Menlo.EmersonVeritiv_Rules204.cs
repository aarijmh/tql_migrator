using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using TQL.BZT.BLL.DataContracts;
using TQL.BZT.Rules.Utils;


namespace TQL.BZT.Rules.Menlo.EmersonVeritiv
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

        }


        /**************************************************************************
         * Customer Values. This section not used in Group Projects.
         * ***********************************************************************/

        /// <summary>
        /// Custom Settings for individual customer
        /// </summary>
        /// <param name="edi204Obj">ref 204 DataContract Object</param>
        /// <returns>204 DataContract Object by Reference</returns>
        public static void setCustomerValues(ref TQLBZTEDI204 edi204Obj)
        {
            try
            {
                //Set Freight Charge to Freight Rate
                edi204Obj.LOADHDR.LOADHDRINFO.FRTRATE = edi204Obj.LOADHDR.LOADHDRINFO.CHARGE;

                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Pick))  //Loop through picks
                {
                    string pickupnumber = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "L11", "BM", ",", "", 0);
                    stop.PICKUPDELIVERYNO = pickupnumber;
                }

                foreach (StopInfo204 stop in Lists.GetSTOPSList(edi204Obj, (int)StopTypes.Drop))  //Loop through drops
                {
                    string deliverypo = TQL.BZT.Rules.Utils.Common.GetAccumulatedStopRefNumbersValue(stop, "", 0, "L11", "PO", ",", "", 0);
                    stop.PICKUPDELIVERYNO = deliverypo;
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
