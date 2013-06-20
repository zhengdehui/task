package cn.dehui.zbj1984105.v201109;

import java.util.ArrayList;
import java.util.List;

import com.google.api.adwords.v201109.cm.KeywordMatchType;
import com.google.api.adwords.v201109.o.CompetitionSearchParameterLevel;
import com.google.api.adwords.v201109.o.DeviceType;
import com.google.api.adwords.v201109.o.DoubleComparisonOperation;
import com.google.api.adwords.v201109.o.LongComparisonOperation;

public class Parameter {

    String                                keyword;

    List<String>                          excludeKeywords      = new ArrayList<String>();

    KeywordMatchType                      keywordMatchType;

    Long                                  languageId;

    Long                                  locationId;

    Integer                               categoryId;

    DeviceType                            deviceType;

    List<CompetitionSearchParameterLevel> competitionLevelList = new ArrayList<CompetitionSearchParameterLevel>();

    LongComparisonOperation               globalMonthlySearchDate;

    LongComparisonOperation               monthlySearchDate;

    DoubleComparisonOperation             adShareData;

    DoubleComparisonOperation             searchShareData;
}
