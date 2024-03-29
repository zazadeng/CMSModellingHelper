--NOTE:
SELECT WCAC.COMPONENTNUM    ,
       WCA.WCAWARDDTLTYPECD ,
       WCA.WCAWARDSTTSTYPECD,
       WCA.AWARDREFNUM      ,
       CC.STARTDT           ,
       CASE PDA.AEDMNTHLYERNGSCD
              WHEN 'NET'
              THEN AECR.NEMTHAMT
              WHEN 'GROSS'
              THEN AECR.GEADJSTDMTHLYAMT
       END MONTHLYEARNINGS                ,
       PDA.AEDMNTHLYERNGSCD               ,
       DATE(PDCAL1.BUSINESSCALCDTM)       ,
       PDCAL1.POSTCPPPDAAMT               ,
       WCA.AWARDEFFECTIVEDT               ,
       DATE(PDCAL2.BUSINESSCALCDTM)       ,
       PDCAL2.POSTCPPPDAAMT               ,
       DATE(FTD.BUSADDEDDTM) APPROVAL_DATE,
       PDA.TOTALDISABILADJPCT             ,
       L.MONTHLYAWARDAMT CURRENT_AMOUNT,
 WCA.workerCmpsnAwardId             ,
       WCAL.SEC2331ASMTRSLTID
FROM   WORKERCMPSNAWARD WCA
       INNER JOIN PERMDISABAWARD PDA
       ON     PDA.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
       AND    WCA.CLAIMID = :CLAIMID
       AND    WCA.RECORDSTATUSCODE   = 'RST1'
       AND    WCA.WCAWARDSTTSTYPECD IN ('WCAS3' ,
                                        'WCAS5' ,
                                        'WCAS7' ,
                                        'WCAS9' ,
                                        'WCAS11',
                                        'WCAS13')
       INNER JOIN WRKCMPSNAWRDCMPNT WCAC
       ON     WCAC.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
       AND    WCAC.RECORDSTATUSCODE   = 'RST1'
       AND    WCAC.WRKCMPSNAWRDCMPNTP IN ('PFX',
                                          'FNC')
       INNER JOIN AEDCALCRESULT AECR
       ON     AECR.DECISIONID = PDA.DECISIONID
       INNER JOIN WCOCASEDECISION WCD
       ON     WCD.DECISIONID = PDA.DECISIONID
       INNER JOIN CLAIMCYCLE CC
       ON     CC.CLAIMCYCLEID     = WCD.CLAIMCYCLEID
       AND    CC.RECORDSTATUSCODE = 'RST1'
       INNER JOIN PDACALCULATION PDCAL1
       ON     PDCAL1.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
       AND    PDCAL1.RECORDSTATUSCODE   = 'RST1'
       AND    PDCAL1.LTDAWDCLCDTETYPECD = 'LTDAD1'
       INNER JOIN PDACALCULATION PDCAL2
       ON     PDCAL2.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
       AND    PDCAL2.RECORDSTATUSCODE   = 'RST1'
       AND    PDCAL2.LTDAWDCLCDTETYPECD = 'LTDAD4'
       INNER JOIN LTDAWARDMTHLYAMT L
       ON     L.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
       AND    L.STARTDT           <= CURRENT_DATE
       AND    L.ENDDT             >= CURRENT_DATE
       AND    L.RECORDSTATUSCODE   = 'RST1'
       LEFT OUTER JOIN FINTRANSDET FTD
       ON     FTD.WORKERCMPSNAWARDID           = WCA.WORKERCMPSNAWARDID
       AND    FTD.WCAWDRSVCOMPTYPECD IS NOT NULL
       LEFT OUTER JOIN WCAS2331ASMTRLNK WCAL
       ON     WCAL.WORKERCMPSNAWARDID = WCA.workerCmpsnAwardId
       AND    WCAL.RECORDSTATUSCODE   = 'RST1'
       AND    WCAL.SEC2331ASMTRSLTID  = :sect2331AssessmentID
WHERE  FTD.FINTRANSDETID                       =
       (SELECT MIN(FTD1.FINTRANSDETID)
       FROM    FINTRANSDET FTD1
       WHERE   FTD1.WORKERCMPSNAWARDID           = WCA.WORKERCMPSNAWARDID
       AND     FTD1.WCAWDRSVCOMPTYPECD IS NOT NULL
       ) 
OR     NOT EXISTS
       (SELECT 1
       FROM    FINTRANSDET FTD2
       WHERE   FTD2.WORKERCMPSNAWARDID           = WCA.WORKERCMPSNAWARDID
       AND     FTD2.WCAWDRSVCOMPTYPECD IS NOT NULL
       )

--RESULT:
--@START
SELECT
	CASE PDA.AEDMNTHLYERNGSCD
  WHEN 'NET' THEN AECR.NEMTHAMT
  WHEN 'GROSS' THEN AECR.GEADJSTDMTHLYAMT
END AS MONTHLYEARNINGS
,	CC.STARTDT
,	DATE(FTD.BUSADDEDDTM) AS APPROVAL_DATE
,	DATE(PDCAL1.BUSINESSCALCDTM)
,	DATE(PDCAL2.BUSINESSCALCDTM)
,	L.MONTHLYAWARDAMT AS CURRENT_AMOUNT
,	PDA.AEDMNTHLYERNGSCD
,	PDA.TOTALDISABILADJPCT
,	PDCAL1.POSTCPPPDAAMT
,	PDCAL2.POSTCPPPDAAMT
,	WCA.AWARDEFFECTIVEDT
,	WCA.AWARDREFNUM
,	WCA.WCAWARDDTLTYPECD
,	WCA.WCAWARDSTTSTYPECD
,	WCA.WORKERCMPSNAWARDID
,	WCAC.COMPONENTNUM
,	WCAL.SEC2331ASMTRSLTID
INTO
	:neMthAmt_MONTHLYEARNINGS@NE_MONTHLY_AMOUNT
,	:startDt_CC@START_DATE
,	:BUSADDEDDTM_APPROVAL_DATE@CURAM_DATE
,	:BUSINESSCALCDTM_PDCAL1@CURAM_DATE
,	:BUSINESSCALCDTM_PDCAL2@CURAM_DATE
,	:monthlyAwardAmt_CURRENT_AMOUNT@CURAM_AMOUNT
,	:aedMnthlyErngsCd_PDA@AED_MONTHLY_EARNINGS_TYPE_CODE
,	:totalDisabilAdjPct_PDA@CMS_PERCENT
,	:postCppPdaAmt_PDCAL1@CURAM_AMOUNT
,	:postCppPdaAmt_PDCAL2@CURAM_AMOUNT
,	:awardEffectiveDt_WCA@AWARD_EFFECTIVE_DT
,	:awardRefNum_WCA@AWARD_REF_NUM
,	:wcAwardDtlTypeCd_WCA@WC_AWARD_DETAIL_TYPE_CODE
,	:wcAwardSttsTypeCd_WCA@WC_AWARD_STTS_TYPE_CD
,	:workerCmpsnAwardId_WCA@WORKER_COMPENSATION_AWARD_ID
,	:componentNum_WCAC@COMPONENT_NUM
,	:sec2331AsmtRsltID_WCAL@SECTION_23_3_1_ASSESSMENT_RESULT_ID 
FROM WORKERCMPSNAWARD AS WCA INNER JOIN PERMDISABAWARD AS PDA ON PDA.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID AND WCA.CLAIMID =:CLAIMIDINPUT_WCA@CLAIM_ID
    AND WCA.RECORDSTATUSCODE =:recordStatusCodeRST1_WCA@RECORD_STATUS_CODE
    AND WCA.WCAWARDSTTSTYPECD IN (:wcAwardSttsTypeCdWCAS3_WCA@WC_AWARD_STTS_TYPE_CD, :wcAwardSttsTypeCdWCAS5_WCA@WC_AWARD_STTS_TYPE_CD, :wcAwardSttsTypeCdWCAS7_WCA@WC_AWARD_STTS_TYPE_CD, :wcAwardSttsTypeCdWCAS9_WCA@WC_AWARD_STTS_TYPE_CD, :wcAwardSttsTypeCdWCAS11_WCA@WC_AWARD_STTS_TYPE_CD, :wcAwardSttsTypeCdWCAS13_WCA@WC_AWARD_STTS_TYPE_CD) INNER JOIN WRKCMPSNAWRDCMPNT AS WCAC ON WCAC.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID AND WCAC.RECORDSTATUSCODE =:recordStatusCodeRST1_WCAC@RECORD_STATUS_CODE
    AND WCAC.WRKCMPSNAWRDCMPNTP IN (:wrkCmpsnAwrdCmpnTpPFX_WCAC@WORKER_COMP_AWARD_CMPNT_TYPE_CODE, :wrkCmpsnAwrdCmpnTpFNC_WCAC@WORKER_COMP_AWARD_CMPNT_TYPE_CODE) INNER JOIN AEDCALCRESULT AS AECR ON AECR.DECISIONID = PDA.DECISIONID INNER JOIN WCOCASEDECISION AS WCD ON WCD.DECISIONID = PDA.DECISIONID INNER JOIN CLAIMCYCLE AS CC ON CC.CLAIMCYCLEID = WCD.CLAIMCYCLEID AND CC.RECORDSTATUSCODE =:recordStatusCodeRST1_CC@RECORD_STATUS_CODE INNER JOIN PDACALCULATION AS PDCAL1 ON PDCAL1.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
    AND PDCAL1.RECORDSTATUSCODE =:recordStatusCodeRST1_PDCAL1@RECORD_STATUS_CODE AND PDCAL1.LTDAWDCLCDTETYPECD =:ltdAwdClcDteTypeCdLTDAD1_PDCAL1@LTD_AWARD_DATE_TYPE_CODE INNER JOIN PDACALCULATION AS PDCAL2 ON PDCAL2.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
    AND PDCAL2.RECORDSTATUSCODE =:recordStatusCodeRST1_PDCAL2@RECORD_STATUS_CODE AND PDCAL2.LTDAWDCLCDTETYPECD =:ltdAwdClcDteTypeCdLTDAD4_PDCAL2@LTD_AWARD_DATE_TYPE_CODE INNER JOIN LTDAWARDMTHLYAMT AS L ON L.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID AND L.STARTDT <= CURRENT_DATE
    AND L.ENDDT >= CURRENT_DATE AND L.RECORDSTATUSCODE =:recordStatusCodeRST1_L@RECORD_STATUS_CODE LEFT OUTER JOIN FINTRANSDET AS FTD ON FTD.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID
    AND FTD.WCAWDRSVCOMPTYPECD IS NOT NULL LEFT OUTER JOIN WCAS2331ASMTRLNK AS WCAL ON WCAL.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID AND WCAL.RECORDSTATUSCODE =:recordStatusCodeRST1_WCAL@RECORD_STATUS_CODE
    AND WCAL.SEC2331ASMTRSLTID =:sect2331AssessmentIDINPUT_WCAL@SECTION_23_3_1_ASSESSMENT_RESULT_ID
  WHERE FTD.FINTRANSDETID = (SELECT MIN(FINTRANSDETID)
                       FROM FINTRANSDET AS FTD1
                       WHERE WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID AND WCAWDRSVCOMPTYPECD IS NOT NULL)
    OR NOT EXISTS (SELECT 1
              FROM FINTRANSDET AS FTD2
              WHERE WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID AND WCAWDRSVCOMPTYPECD IS NOT NULL)
--@END