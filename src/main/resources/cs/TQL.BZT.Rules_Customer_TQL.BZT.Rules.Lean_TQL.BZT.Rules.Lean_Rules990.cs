using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using TQL.BZT.BLL.DataContracts;
using TQL.BZT.Rules.Utils;

namespace TQL.BZT.Rules.Lean
{
    public class Rules990
    {
        /// <summary>
        /// Common 990 Settings for the group.
        /// ExtraCode to be added.But code not to be removed from Group settings.
        /// </summary>
        /// <param name="edi990Obj">990 DataContract Object</param>
        /// <returns>990 DataContract Object</returns>
        public static void setGroupValues(ref TQLBZTEDI990 edi990Obj)
        {
            //add group settings code here.
            try
            {

                //Set Custom include/exclude flag. RuleType = 8
                RulesInfoCollection rulesinfocollection = new RulesInfoCollection();
                rulesinfocollection = edi990Obj.LOADHDR.RULEINFOCollection;

                Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "G62", "F");
                Common.UpdateIncludeSegmentRule(ref rulesinfocollection, "V9", "F");

                /*******    Need to set the header ref number N9*TN   ***********/
                //Default will have on N9 record with the CN qualifier. Change to TN qualifier


                //RefNumberInfoCollection trefnumbers = new RefNumberInfoCollection();
                //RefNumberInfo trefnumber = new RefNumberInfo();
                
                //Update RefNumber with L11, TN Qualifier with destination "N9".  Unset base map's "N9" value

                foreach (RefNumberInfo refnumber in edi990Obj.LOADHDR.REFNUMBERINFOCollection.Items)
                {

                    if (refnumber.EDISOURCE.Trim().ToUpper().IndexOf("L11") >= 0 && refnumber.REFNUMBERQUAL.Trim ().ToUpper() == "TN" )
                    {
                        refnumber.EDI990DESTINATION = "N9";
                    }
                    else
                    {
                        refnumber.EDI990DESTINATION = "";
                    }
                }

                //edi990Obj.LOADHDR.REFNUMBERINFOCollection = trefnumbers;
            }
            catch (Exception e)
            {
                throw e;
            }      
        }
        /// <summary>
        /// Custom Settings for individual customer
        /// </summary>
        /// <param name="edi990Obj">ref 990 DataContract Object</param>
        /// <returns>990 DataContract Object by Reference</returns>
        public static void setCustomerValues(ref TQLBZTEDI990 edi990Obj)
        {
            //add code here

        }


    }
}
