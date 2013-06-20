// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package cn.dehui.zbj1984105;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.google.api.adwords.lib.AdWordsService;
import com.google.api.adwords.lib.AdWordsServiceLogger;
import com.google.api.adwords.lib.AdWordsUser;
import com.google.api.adwords.lib.utils.MapUtils;
import com.google.api.adwords.v201209.cm.Paging;
import com.google.api.adwords.v201209.o.Attribute;
import com.google.api.adwords.v201209.o.AttributeType;
import com.google.api.adwords.v201209.o.IdeaType;
import com.google.api.adwords.v201209.o.IntegerSetAttribute;
import com.google.api.adwords.v201209.o.LongAttribute;
import com.google.api.adwords.v201209.o.RelatedToQuerySearchParameter;
import com.google.api.adwords.v201209.o.RequestType;
import com.google.api.adwords.v201209.o.SearchParameter;
import com.google.api.adwords.v201209.o.StringAttribute;
import com.google.api.adwords.v201209.o.TargetingIdea;
import com.google.api.adwords.v201209.o.TargetingIdeaPage;
import com.google.api.adwords.v201209.o.TargetingIdeaSelector;
import com.google.api.adwords.v201209.o.TargetingIdeaServiceInterface;

/**
 * This example gets keywords related to a seed keyword.
 *
 * Tags: TargetingIdeaService.get
 *
 * @category adx-exclude
 * @author api.arogal@gmail.com (Adam Rogal)
 */
public class GetRelatedKeywords {
    public static void main(String[] args) {
        try {
            // Log SOAP XML request and response.
            AdWordsServiceLogger.log();

            // Get AdWordsUser from "~/adwords.properties".
            AdWordsUser user = new AdWordsUser("adwords.properties");

            // Get the TargetingIdeaService.
            TargetingIdeaServiceInterface targetingIdeaService = user
                    .getService(AdWordsService.V201209.TARGETING_IDEA_SERVICE);

            // Create selector.
            TargetingIdeaSelector selector = new TargetingIdeaSelector();
            selector.setRequestType(RequestType.IDEAS);
            selector.setIdeaType(IdeaType.KEYWORD);
            selector.setRequestedAttributeTypes(new AttributeType[] { AttributeType.KEYWORD_TEXT,
                    AttributeType.SEARCH_VOLUME, AttributeType.CATEGORY_PRODUCTS_AND_SERVICES });

            // Set selector paging (required for targeting idea service).
            Paging paging = new Paging();
            paging.setStartIndex(0);
            paging.setNumberResults(10);
            selector.setPaging(paging);

            // Create related to query search parameter.
            RelatedToQuerySearchParameter relatedToQuerySearchParameter = new RelatedToQuerySearchParameter();
            relatedToQuerySearchParameter.setQueries(new String[] { "mars cruise" });
            selector.setSearchParameters(new SearchParameter[] { relatedToQuerySearchParameter });

            // Get related keywords.
            TargetingIdeaPage page = targetingIdeaService.get(selector);

            // Display related keywords.
            if (page.getEntries() != null && page.getEntries().length > 0) {
                for (TargetingIdea targetingIdea : page.getEntries()) {
                    Map<AttributeType, Attribute> data = MapUtils.toMap(targetingIdea.getData());
                    StringAttribute keyword = (StringAttribute) data.get(AttributeType.KEYWORD_TEXT);
                    IntegerSetAttribute categories = (IntegerSetAttribute) data
                            .get(AttributeType.CATEGORY_PRODUCTS_AND_SERVICES);
                    String categoriesString = "(none)";
                    if (categories != null && categories.getValue() != null) {
                        categoriesString = StringUtils.join(ArrayUtils.toObject(categories.getValue()), ", ");
                    }
                    Long averageMonthlySearches = ((LongAttribute) data.get(AttributeType.SEARCH_VOLUME)).getValue();
                    System.out.println("Keyword with text '" + keyword.getValue()
                            + "' and average monthly search volume '" + averageMonthlySearches
                            + "' was found with categories: " + categoriesString);
                }
            } else {
                System.out.println("No related keywords were found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
