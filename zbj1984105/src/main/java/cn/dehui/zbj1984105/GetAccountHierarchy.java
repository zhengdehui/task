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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.api.adwords.lib.AdWordsService;
import com.google.api.adwords.lib.AdWordsServiceLogger;
import com.google.api.adwords.lib.AdWordsUser;
import com.google.api.adwords.v201209.cm.Selector;
import com.google.api.adwords.v201209.mcm.ManagedCustomer;
import com.google.api.adwords.v201209.mcm.ManagedCustomerLink;
import com.google.api.adwords.v201209.mcm.ManagedCustomerPage;
import com.google.api.adwords.v201209.mcm.ManagedCustomerServiceInterface;

/**
 * This example gets the account hierarchy under the current account.
 *
 * Tags: ManagedCustomerService.get
 *
 * @category adx-exclude
 * @author api.arogal@gmail.com (Adam Rogal)
 */
public class GetAccountHierarchy {
    public static void main(String[] args) {
        try {
            // Log SOAP XML request and response.
            AdWordsServiceLogger.log();

            // Get AdWordsUser from "~/adwords.properties".
            AdWordsUser user = new AdWordsUser("adwords.properties").generateClientAdWordsUser(null);

            // Get the ServicedAccountService.
            ManagedCustomerServiceInterface managedCustomerService = user
                    .getService(AdWordsService.V201209.MANAGED_CUSTOMER_SERVICE);

            // Create selector.
            Selector selector = new Selector();
            selector.setFields(new String[] { "Login", "CustomerId" });

            // Get results.
            ManagedCustomerPage page = managedCustomerService.get(selector);

            if (page.getEntries() != null) {
                // Create map from customerId to customer node.
                Map<Long, ManagedCustomerTreeNode> customerIdToCustomerNode = new HashMap<Long, ManagedCustomerTreeNode>();

                // Create account tree nodes for each customer.
                for (ManagedCustomer customer : page.getEntries()) {
                    ManagedCustomerTreeNode node = new ManagedCustomerTreeNode();
                    node.managedCustomer = customer;
                    customerIdToCustomerNode.put(customer.getCustomerId(), node);
                }

                // For each link, connect nodes in tree.
                if (page.getLinks() != null) {
                    for (ManagedCustomerLink link : page.getLinks()) {
                        ManagedCustomerTreeNode managerNode = customerIdToCustomerNode.get(link.getManagerCustomerId());
                        ManagedCustomerTreeNode childNode = customerIdToCustomerNode.get(link.getClientCustomerId());
                        childNode.parentNode = managerNode;
                        if (managerNode != null) {
                            managerNode.childAccounts.add(childNode);
                        }
                    }
                }

                // Find the root account node in the tree.
                ManagedCustomerTreeNode rootNode = null;
                for (ManagedCustomer account : page.getEntries()) {
                    if (customerIdToCustomerNode.get(account.getCustomerId()).parentNode == null) {
                        rootNode = customerIdToCustomerNode.get(account.getCustomerId());
                        break;
                    }
                }

                // Display account tree.
                System.out.println("Login, CustomerId (Status)");
                System.out.println(rootNode.toTreeString(0, new StringBuffer()));
            } else {
                System.out.println("No serviced accounts were found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Example implementation of a node that would exist in an account tree.
     */
    private static class ManagedCustomerTreeNode {
        protected ManagedCustomerTreeNode       parentNode;

        protected ManagedCustomer               managedCustomer;

        protected List<ManagedCustomerTreeNode> childAccounts = new ArrayList<ManagedCustomerTreeNode>();

        /**
         * Default constructor.
         */
        public ManagedCustomerTreeNode() {
        }

        @Override
        public String toString() {
            String login = managedCustomer.getLogin();
            if (login == null || login.trim().length() < 1) {
                login = "(no login)";
            }
            return String.format("%s, %s", login, managedCustomer.getCustomerId());
        }

        /**
         * Returns a string representation of the current level of the tree and recursively returns the
         * string representation of the levels below it.
         *
         * @param depth the depth of the node
         * @param sb the string buffer containing the tree representation
         * @return the tree string representation
         */
        public StringBuffer toTreeString(int depth, StringBuffer sb) {
            sb.append(StringUtils.repeat("-", depth * 2)).append(this).append("\n");
            for (ManagedCustomerTreeNode childAccount : childAccounts) {
                childAccount.toTreeString(depth + 1, sb);
            }
            return sb;
        }
    }
}
