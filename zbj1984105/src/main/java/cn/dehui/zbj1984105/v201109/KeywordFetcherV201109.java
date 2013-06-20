package cn.dehui.zbj1984105.v201109;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cn.dehui.zbj1984105.KeywordRollingFileAppender;

import com.google.api.adwords.lib.AdWordsService;
import com.google.api.adwords.lib.AdWordsUser;
import com.google.api.adwords.lib.utils.MapUtils;
import com.google.api.adwords.v201109.cm.Keyword;
import com.google.api.adwords.v201109.cm.KeywordMatchType;
import com.google.api.adwords.v201109.cm.Language;
import com.google.api.adwords.v201109.cm.Location;
import com.google.api.adwords.v201109.cm.Paging;
import com.google.api.adwords.v201109.o.AdShareSearchParameter;
import com.google.api.adwords.v201109.o.Attribute;
import com.google.api.adwords.v201109.o.AttributeType;
import com.google.api.adwords.v201109.o.AverageTargetedMonthlySearchesSearchParameter;
import com.google.api.adwords.v201109.o.CategoryProductsAndServicesSearchParameter;
import com.google.api.adwords.v201109.o.CompetitionSearchParameter;
import com.google.api.adwords.v201109.o.CompetitionSearchParameterLevel;
import com.google.api.adwords.v201109.o.CriterionAttribute;
import com.google.api.adwords.v201109.o.DeviceTypeSearchParameter;
import com.google.api.adwords.v201109.o.DoubleAttribute;
import com.google.api.adwords.v201109.o.ExcludedKeywordSearchParameter;
import com.google.api.adwords.v201109.o.GlobalMonthlySearchesSearchParameter;
import com.google.api.adwords.v201109.o.IdeaType;
import com.google.api.adwords.v201109.o.KeywordMatchTypeSearchParameter;
import com.google.api.adwords.v201109.o.LanguageSearchParameter;
import com.google.api.adwords.v201109.o.LocationSearchParameter;
import com.google.api.adwords.v201109.o.LongAttribute;
import com.google.api.adwords.v201109.o.RelatedToKeywordSearchParameter;
import com.google.api.adwords.v201109.o.RequestType;
import com.google.api.adwords.v201109.o.SearchParameter;
import com.google.api.adwords.v201109.o.SearchShareSearchParameter;
import com.google.api.adwords.v201109.o.TargetingIdea;
import com.google.api.adwords.v201109.o.TargetingIdeaPage;
import com.google.api.adwords.v201109.o.TargetingIdeaSelector;
import com.google.api.adwords.v201109.o.TargetingIdeaServiceInterface;

public class KeywordFetcherV201109 implements Runnable {

    public static int                     limit  = 20;

    private Logger                        logger = Logger.getLogger(getClass());

    private int                           startIndex;

    private int                           count;

    private int                           threadCount;

    private Parameter                     parameter;

    private TargetingIdeaServiceInterface targetingIdeaService;

    private TargetingIdeaSelector         selector;

    private CountDownLatch                startSignal;

    private CountDownLatch                doneSignal;

    //    static {
    //        initLogger("keywords.csv");
    //    }

    public static void initLogger(String filePath, String maxLinePerFile) {

        Properties logProperties = new Properties();

        //        logProperties.put("log4j.appender.keywordCsv", "org.apache.log4j.FileAppender");
        logProperties.put("log4j.appender.keywordCsv", KeywordRollingFileAppender.class.getName());
        logProperties.put("log4j.appender.keywordCsv.File", filePath);
        logProperties.put("log4j.appender.keywordCsv.MaxLine", maxLinePerFile);
        //        logProperties.put("log4j.appender.keywordCsv.MaxBackupIndex", String.valueOf(Integer.MAX_VALUE));
        logProperties.put("log4j.appender.keywordCsv.layout", "org.apache.log4j.PatternLayout");
        logProperties.put("log4j.appender.keywordCsv.layout.ConversionPattern", "%m%n");

        logProperties.put("log4j.appender.keywordConsole", "org.apache.log4j.ConsoleAppender");
        logProperties.put("log4j.appender.keywordConsole.layout", "org.apache.log4j.PatternLayout");
        logProperties.put("log4j.appender.keywordConsole.layout.ConversionPattern", "%m%n");

        logProperties.put("log4j.rootLogger", "DEBUG");
        logProperties.put("log4j.logger.cn.dehui.zbj1984105", "INFO,keywordCsv,keywordConsole");
        //        logProperties.put("log4j.logger.cn.dehui.zbj1984105.KeywordFetcher", "INFO,keywordConsole");

        PropertyConfigurator.configure(logProperties);
    }

    public KeywordFetcherV201109(Parameter parameter, int startIndex, int count, int threadCount,
            CountDownLatch startSignal, CountDownLatch doneSignal) throws IOException, ServiceException {
        this(parameter, startIndex, count, threadCount, startSignal, doneSignal,
                (TargetingIdeaServiceInterface) new AdWordsUser("adwords.properties")
                        .getService(AdWordsService.V201109.TARGETING_IDEA_SERVICE));
    }

    public KeywordFetcherV201109(Parameter parameter, int startIndex, int count, int threadCount,
            CountDownLatch startSignal, CountDownLatch doneSignal, TargetingIdeaServiceInterface targetingIdeaService)
            throws IOException, ServiceException {
        this.parameter = parameter;
        this.startIndex = startIndex;
        this.count = count;
        this.threadCount = threadCount;

        this.startSignal = startSignal;
        this.doneSignal = doneSignal;

        buildSelector();

        this.targetingIdeaService = targetingIdeaService;

        //        AdWordsUser user = new AdWordsUser();
        //        targetingIdeaService = user.getService(AdWordsService.V201109.TARGETING_IDEA_SERVICE);
    }

    private void buildSelector() {
        selector = new TargetingIdeaSelector();
        selector.setRequestType(RequestType.IDEAS);
        selector.setIdeaType(IdeaType.KEYWORD);
        selector.setRequestedAttributeTypes(new AttributeType[] { AttributeType.CRITERION, AttributeType.COMPETITION,
                AttributeType.GLOBAL_MONTHLY_SEARCHES, AttributeType.AVERAGE_TARGETED_MONTHLY_SEARCHES });

        // Create seed keyword.
        Keyword keyword = new Keyword();
        keyword.setText(parameter.keyword);
        keyword.setMatchType(parameter.keywordMatchType);

        List<SearchParameter> searchParameterList = new ArrayList<SearchParameter>();

        // keyword
        RelatedToKeywordSearchParameter relatedToKeywordSearchParameter = new RelatedToKeywordSearchParameter();
        relatedToKeywordSearchParameter.setKeywords(new Keyword[] { keyword });
        searchParameterList.add(relatedToKeywordSearchParameter);

        // exclude keywords
        if (!parameter.excludeKeywords.isEmpty()) {
            ExcludedKeywordSearchParameter excludedKeywordSearchParameter = new ExcludedKeywordSearchParameter();

            List<Keyword> keywords = new ArrayList<Keyword>();
            for (int i = 0; i < parameter.excludeKeywords.size(); i++) {
                if (!parameter.excludeKeywords.get(i).trim().isEmpty()) {
                    Keyword k = new Keyword();
                    k.setText(parameter.excludeKeywords.get(i).trim());
                    k.setMatchType(KeywordMatchType.EXACT);

                    keywords.add(k);
                }
            }
            excludedKeywordSearchParameter.setKeywords(keywords.toArray(new Keyword[keywords.size()]));
            searchParameterList.add(excludedKeywordSearchParameter);
        }

        // match type
        KeywordMatchTypeSearchParameter keywordMatchTypeSearchParameter = new KeywordMatchTypeSearchParameter();
        keywordMatchTypeSearchParameter.setKeywordMatchTypes(new KeywordMatchType[] { parameter.keywordMatchType });
        searchParameterList.add(keywordMatchTypeSearchParameter);

        // language
        if (parameter.languageId != null) {
            LanguageSearchParameter languageSearchParameter = new LanguageSearchParameter();
            languageSearchParameter.setLanguages(new Language[] { new Language(parameter.languageId, null, null, null,
                    null) });
            searchParameterList.add(languageSearchParameter);
        }

        // location
        if (parameter.locationId != null) {
            LocationSearchParameter locationSearchParameter = new LocationSearchParameter();
            locationSearchParameter.setLocations(new Location[] { new Location(parameter.locationId, null, null, null,
                    null, null, null) });
            searchParameterList.add(locationSearchParameter);
        }

        // category
        if (parameter.categoryId != null) {
            CategoryProductsAndServicesSearchParameter categoryProductsAndServicesSearchParameter = new CategoryProductsAndServicesSearchParameter();
            categoryProductsAndServicesSearchParameter.setCategoryId(parameter.categoryId);
            searchParameterList.add(categoryProductsAndServicesSearchParameter);
        }

        // device type
        DeviceTypeSearchParameter deviceTypeSearchParameter = new DeviceTypeSearchParameter();
        deviceTypeSearchParameter.setDeviceType(parameter.deviceType);
        searchParameterList.add(deviceTypeSearchParameter);

        // Competition Search
        if (!parameter.competitionLevelList.isEmpty()) {
            CompetitionSearchParameter competitionSearchParameter = new CompetitionSearchParameter();
            competitionSearchParameter.setLevels(parameter.competitionLevelList
                    .toArray(new CompetitionSearchParameterLevel[parameter.competitionLevelList.size()]));
            searchParameterList.add(competitionSearchParameter);
        }

        // Global Monthly Searches
        if (parameter.globalMonthlySearchDate != null) {
            GlobalMonthlySearchesSearchParameter globalMonthlySearchesSearchParameter = new GlobalMonthlySearchesSearchParameter();
            globalMonthlySearchesSearchParameter.setOperation(parameter.globalMonthlySearchDate);
            searchParameterList.add(globalMonthlySearchesSearchParameter);
        }

        // Average Targeted Monthly Searches
        if (parameter.monthlySearchDate != null) {
            AverageTargetedMonthlySearchesSearchParameter averageTargetedMonthlySearchesSearchParameter = new AverageTargetedMonthlySearchesSearchParameter();
            averageTargetedMonthlySearchesSearchParameter.setOperation(parameter.monthlySearchDate);
            searchParameterList.add(averageTargetedMonthlySearchesSearchParameter);
        }

        // Ad Share
        if (parameter.adShareData != null) {
            AdShareSearchParameter adShareSearchParameter = new AdShareSearchParameter();
            adShareSearchParameter.setOperation(parameter.adShareData);
            searchParameterList.add(adShareSearchParameter);
        }

        // Search Share
        if (parameter.searchShareData != null) {
            SearchShareSearchParameter searchShareSearchParameter = new SearchShareSearchParameter();
            searchShareSearchParameter.setOperation(parameter.searchShareData);
            searchParameterList.add(searchShareSearchParameter);
        }

        selector.setSearchParameters(searchParameterList.toArray(new SearchParameter[searchParameterList.size()]));
    }

    public void run() {
        try {
            startSignal.await();

            int offset = 0;
            while (true) {
                // Set selector paging (required for targeting idea service).
                Paging paging = new Paging();
                int index = startIndex + offset * threadCount * count;
                if (index >= limit) {
                    System.out.printf("Reach LIMIT, index: %d", index);
                    break;
                }
                paging.setStartIndex(index);
                paging.setNumberResults(count);
                selector.setPaging(paging);

                // Get related keywords.
                TargetingIdeaPage page;
                page = targetingIdeaService.get(selector);

                // Display related keywords.
                if (page.getEntries() != null && page.getEntries().length > 0) {
                    for (TargetingIdea targetingIdea : page.getEntries()) {
                        String tpl = "%s,%f,%d,%d";

                        Map<AttributeType, Attribute> data = MapUtils.toMap(targetingIdea.getData());

                        Keyword keyword = (Keyword) ((CriterionAttribute) data.get(AttributeType.CRITERION)).getValue();

                        Double competition = ((DoubleAttribute) data.get(AttributeType.COMPETITION)).getValue();

                        Long globalMonthlySearches = ((LongAttribute) data.get(AttributeType.GLOBAL_MONTHLY_SEARCHES))
                                .getValue();

                        Long averageMonthlySearches = ((LongAttribute) data
                                .get(AttributeType.AVERAGE_TARGETED_MONTHLY_SEARCHES)).getValue();

                        //                    Long targetedMonthlySearches = ((LongAttribute) data.get(AttributeType.TARGETED_MONTHLY_SEARCHES))
                        //                            .getValue();

                        String log = String.format(tpl, keyword.getText(), competition, globalMonthlySearches,
                                averageMonthlySearches);
                        logger.info(log);
                    }
                    offset++;
                } else {
                    System.out.println("No related keywords were found.");
                    break;
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            doneSignal.countDown();
        }
    }
}
