package cn.dehui.zbj1984105.v201209;

import java.util.ArrayList;
import java.util.List;

import com.google.api.adwords.v201209.cm.KeywordMatchType;
import com.google.api.adwords.v201209.o.CompetitionSearchParameterLevel;
import com.google.api.adwords.v201209.o.DeviceType;
import com.google.api.adwords.v201209.o.DoubleComparisonOperation;
import com.google.api.adwords.v201209.o.LongComparisonOperation;

public class Parameter {

    String                                keyword;

    List<String>                          excludeKeywords      = new ArrayList<String>();

    KeywordMatchType                      keywordMatchType;

    Long                                  languageId           = null;

    Long                                  locationId           = null;

    Integer                               categoryId;

    DeviceType                            deviceType;

    List<CompetitionSearchParameterLevel> competitionLevelList = new ArrayList<CompetitionSearchParameterLevel>();

    LongComparisonOperation               globalMonthlySearchData;

    LongComparisonOperation               monthlySearchData;

    DoubleComparisonOperation             adShareData;

    DoubleComparisonOperation             searchShareData;
}
