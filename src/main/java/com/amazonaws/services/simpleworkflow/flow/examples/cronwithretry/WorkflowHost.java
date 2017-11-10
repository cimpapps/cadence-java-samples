/*
 * Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.simpleworkflow.flow.examples.cronwithretry;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.uber.cadence.WorkflowService;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.amazonaws.services.simpleworkflow.flow.examples.common.ConfigHelper;

public class WorkflowHost {

    public static final String DECISION_TASK_LIST = "PeriodicWorkflow";

    public static void main(String[] args) throws Exception {
        ConfigHelper configHelper = ConfigHelper.createConfig();
        WorkflowService.Iface swfService = configHelper.createWorkflowClient();
        String domain = configHelper.getDomain();

        final WorkflowWorker worker = new WorkflowWorker(swfService, domain, DECISION_TASK_LIST);
        worker.addWorkflowImplementationType(CronWithRetryWorkflowImpl.class);
        worker.setRegisterDomain(true);
        worker.setDomainRetentionPeriodInDays(1);
        worker.start();

        System.out.println("Workflow Host Service Started...");

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                try {
                    worker.shutdownAndAwaitTermination(1, TimeUnit.MINUTES);
                    System.out.println("Workflow Host Service Terminated...");
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Please press any key to terminate service.");

        try {
            System.in.read();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);

    }
}
