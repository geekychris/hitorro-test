/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.test.transformer;

import com.hitorro.basedms.BaseUtil;
import com.hitorro.basedms.cache.ContentTypeCache;
import com.hitorro.basedms.contentconstraints.FileNameMatchContentConstraint;
import com.hitorro.basedms.job.JobService;
import com.hitorro.basedms.queue.QueueUtil;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.basedms.transformer.ConvertionEdge;
import com.hitorro.basedms.transformer.TransformJob;
import com.hitorro.basedms.transformer.TransformJobParameters;
import com.hitorro.basedms.transformer.TransformerService;
import com.hitorro.basedms.transformer.constraints.ToConstraint;
import com.hitorro.basedms.transformer.squeeze.SqueezeService;
import com.hitorro.basedms.workflow.WorkFlowItem;
import com.hitorro.basedms.workflow.WorkFlowItemEntry;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Platform;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.job.DecisionJobParameters;
import com.hitorro.util.job.JobExecutionResult;
import com.hitorro.util.job.JobParameters;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;


@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Test transformation services",
        platform = {Platform.MacOSX})
public class DocTransformerTest extends EnhancedTestCase {
    public void tsestDecision() {
        com.hitorro.util.typesystem.BaseSession session = DMSSessionFactory.getFactory().getSession();
        try {
            WorkFlowItem wfi = new WorkFlowItem();
            wfi.setFinished(false);
            wfi.setStateMachine("producerapp");
            wfi.setCurrentState("Initial");
            wfi.addEntry("approvable", "foo");
            session.persist(wfi);
            session.commit();

            DecisionJobParameters d = new DecisionJobParameters();
            d.setNotifyGuid(wfi.getGuid());
            d.setNotifyGuidState(wfi.getCurrentState());

            d.getChoices().add("approve");
            d.getChoices().add("deny");
            d.getChoicesDisplayStrings().add("Approve this upload document");
            d.getChoicesDisplayStrings().add("Deny this upload document");
            d.setDescription("chris, please review this upload doc.");
            d.setPayloadGuid("payloadGuid");
            //QueueUtil.enqueJob(d, session, true, PersistedSerializedObject.CollectionID_UIJobQueue);
            com.hitorro.base.objects.PersistedSerializedObject pso = getPSO(session, d);
            session.commit();
            try {
                JobExecutionResult jer = BaseUtil.executeJob(session, pso);
                if (jer.getErrorLevel().equals(Level.INFO)) {
                    session.commit();
                }
            } catch (SQLException e) {
                Log.util.error("%s %e", e, e);
            } catch (IOException e) {
                Log.util.error("%s %e", e, e);
            } catch (StoreException e) {
                Log.util.error("%s %e", e, e);
            } catch (ClassNotFoundException e) {
                Log.util.error("%s %e", e, e);
            }

            session.commit();
            //String wfiGuid = wfi.getGuid();
            //wfi.notify(wfi, session);

        } finally {
            DMSSessionFactory.getFactory().rollbackClose(session);
        }

    }

    public com.hitorro.base.objects.PersistedSerializedObject getPSO(com.hitorro.util.typesystem.BaseSession session, com.hitorro.util.typesystem.HTSerializable p) {
        com.hitorro.base.objects.PersistedSerializedObject pso = new com.hitorro.base.objects.PersistedSerializedObject();

        pso.setEffectiveFrom(new Date());

        pso.setName(JobService.PSO_JOB_NAME);
        try {
            pso.setSerializableObject(p);
        } catch (IOException e) {
            return null;
        } catch (StoreException e) {
            return null;
        }
        pso.setCollectionId(com.hitorro.base.objects.PersistedSerializedObject.CollectionID_UIJobQueue);
        session.persist(pso);
        return pso;
    }

    public void tdsestElement() {
        com.hitorro.util.typesystem.BaseSession session = DMSSessionFactory.getFactory().getSession();
        com.hitorro.util.typesystem.BaseSession session2 = DMSSessionFactory.getFactory().getSession();
        try {
            WorkFlowItem wfi = new WorkFlowItem();
            wfi.setFinished(false);
            wfi.setStateMachine("producerapp");
            wfi.setCurrentState("Initial");
            wfi.addEntry("approvable", "foo");
            wfi.addEntry("foo", "bla");
            wfi.addEntry("foo2", "bla2");
            session.persist(wfi);
            session.commit();
            String wfiGuid = wfi.getGuid();
            WorkFlowItem wfi2 = (WorkFlowItem) session2.getObjectFromGuid(wfiGuid);
            Set<WorkFlowItemEntry> set = wfi2.getWorkflowItemEntries();
            for (WorkFlowItemEntry entry : set) {
                Console.println("%s: %s", entry.getItemName(), entry.getItemGuid());
            }
            Console.println("Hello");

        } finally {
            DMSSessionFactory.getFactory().rollbackClose(session);
        }

    }

    public void test() {
        com.hitorro.util.typesystem.BaseSession session = DMSSessionFactory.getFactory().getSession();
        try {
            WorkFlowItem wfi = new WorkFlowItem();
            wfi.setFinished(false);
            wfi.setStateMachine("producerapp");
            wfi.setCurrentState("Initial");
            wfi.addEntry("approvable", "foo");
            session.persist(wfi);
            session.commit();
            String wfiGuid = wfi.getGuid();

            DecisionJobParameters d = new DecisionJobParameters();
            d.setNotifyGuid(wfi.getGuid());
            d.setNotifyGuidState(wfi.getCurrentState());

            d.getChoices().add("approve");
            d.getChoices().add("deny");
            d.setChoice("approve");
            d.getChoicesDisplayStrings().add("Approve this upload document");
            d.getChoicesDisplayStrings().add("Deny this upload document");
            d.setDescription("chris, please review this upload doc.");
            d.setPayloadGuid("payloadGuid");
            //QueueUtil.enqueJob(d, session, true, PersistedSerializedObject.CollectionID_UIJobQueue);
            com.hitorro.base.objects.PersistedSerializedObject pso = getPSO(session, d);
            session.commit();


            wfi.notify(d, session);

        } finally {
            DMSSessionFactory.getFactory().rollbackClose(session);
        }

    }

    public void tdestQueue() throws IOException, StoreException {
        if (SqueezeService.getService() == null) {
            return;
        }


        com.hitorro.util.typesystem.BaseSession session = DMSSessionFactory.getFactory().getSession();
        try {
            WorkFlowItem wfi = new WorkFlowItem();
            BaseFile hilary = this.getInputFileRelativeBaseFile("hillary.wmv");
            assertFileExistsWithExplanation(hilary);
            com.hitorro.base.objects.VersionableObject testObject = new com.hitorro.base.objects.VersionableObject();
            com.hitorro.base.objects.ContentType ct = ContentTypeCache.getCache().getContentTypeByMimeType("video/x-ms-wmv");
            assertNotNull(ct);
            String hilaryFile = "hillary.wmv";
            testObject.setContent(hilaryFile, ct, hilary);
            session.persist(testObject);
            session.commit();
            String guid = testObject.getGuid();

            // now let try to convertToPdf it.
            TransformJobParameters params = new TransformJobParameters();
            params.provisionId();
            HTPredicate<com.hitorro.base.objects.Content> cc = new FileNameMatchContentConstraint(hilaryFile, true);

            params.setContentConstraint(cc);
            params.setJobGuid(guid);
            com.hitorro.base.objects.ContentSetter setter = new com.hitorro.base.objects.ContentSetter();

            TransformerService s = TransformerService.getService();
            ToConstraint toConst = new ToConstraint("application/x-shockwave-flash");
            List<ConvertionEdge> edges = TransformerService.getService().getConvertionContext().visit(toConst);
            if (ListUtil.nullOrEmpty(edges)) {
                assertAlways("No edges found for flash");
            }
            params.setTransformerMethod(edges.get(0).getTransformerMethod());
            setter.setMimeType("application/x-shockwave-flash");
            setter.setSysGuid(guid);
            setter.setFileName("hillary.fla");
            params.setContentSetter(setter);

            wfi.setStateMachine("uploadworkflow");
            wfi.setCurrentState("initial");
            wfi.addEntry("approvable", guid);
            session.persist(wfi);
            session.commit();
            String wfiGuid = wfi.getGuid();
            params.setNotifyGuid(wfiGuid);
            params.setNotifyGuidState("initial");

            QueueUtil.enqueJob(params, session, true, com.hitorro.base.objects.PersistedSerializedObject.CollectionID_TranscoderQueue, 0);
        } finally {
            DMSSessionFactory.getFactory().rollbackClose(session);
        }
    }


    public void teest() throws IOException, StoreException, ClassNotFoundException {
        if (SqueezeService.getService() == null) {
            return;
        }


        com.hitorro.util.typesystem.BaseSession session = DMSSessionFactory.getFactory().getSession();
        try {
            BaseFile hilary = this.getInputFileRelativeBaseFile("hillary.wmv");
            assertFileExistsWithExplanation(hilary);
            com.hitorro.base.objects.VersionableObject testObject = new com.hitorro.base.objects.VersionableObject();
            com.hitorro.base.objects.ContentType ct = ContentTypeCache.getCache().getContentTypeByMimeType("video/x-ms-wmv");
            assertNotNull(ct);
            String hilaryFile = "hillary.wmv";
            testObject.setContent(hilaryFile, ct, hilary);
            session.persist(testObject);
            session.commit();
            String guid = testObject.getGuid();

            // now let try to convertToPdf it.
            TransformJobParameters params = new TransformJobParameters();
            params.provisionId();
            HTPredicate<com.hitorro.base.objects.Content> cc = new FileNameMatchContentConstraint(hilaryFile, true);

            params.setContentConstraint(cc);
            params.setJobGuid(guid);
            com.hitorro.base.objects.ContentSetter setter = new com.hitorro.base.objects.ContentSetter();

            TransformerService s = TransformerService.getService();
            ToConstraint toConst = new ToConstraint("application/x-shockwave-flash");
            List<ConvertionEdge> edges = TransformerService.getService().getConvertionContext().visit(toConst);
            if (ListUtil.nullOrEmpty(edges)) {
                assertAlways("No edges found for flash");
            }
            params.setTransformerMethod(edges.get(0).getTransformerMethod());
            setter.setMimeType("application/x-shockwave-flash");
            setter.setSysGuid(guid);
            setter.setFileName("hillary.fla");
            params.setContentSetter(setter);

            //DMSSession session, HTBaseObjectOutputStream dos, TypeManager mgr, boolean includeContent
            File f = this.getOutputFileRelative("ser.ser");
            OutputStream dos = FileUtil.getBufferedFileOutputStream(f);
            com.hitorro.util.typesystem.HTObjectOutputStream poos = new com.hitorro.util.typesystem.HTObjectOutputStreamImpl(null, dos, com.hitorro.util.typesystem.TypeManager.getTypeManager(), false);

            poos.writeVersionedObject(params);
            poos.flush();
            dos.close();

            ObjectInputStream dis = FileUtil.getObjectInputStreamForFile(f);

            com.hitorro.util.typesystem.HTObjectInputStream pis = new com.hitorro.util.typesystem.HTObjectInputStreamImpl(dis, com.hitorro.util.typesystem.TypeManager.getTypeManager(),
                    null, com.hitorro.util.typesystem.HTObjectOutputStream.LoadType.IgnoreDupes, false);
            JobParameters params2 = (JobParameters) pis.readVersionedObject();
            assertNotNull(params2);
            TransformJob job = new TransformJob();
            job.setSession(session);
            JobExecutionResult jr = job.doAction(params2);
            com.hitorro.base.objects.VersionableObject so = (com.hitorro.base.objects.VersionableObject) session.getObjectFromGuid(guid);
            Set<com.hitorro.base.objects.Content> contents = so.getContents();
            for (com.hitorro.base.objects.Content c : contents) {
                BaseFile cf = c.getContentFile();
                long size = 0;
                if (BaseFile.notNullAndExists(cf)) {
                    size = cf.length();
                }
                Console.println("name: %s type: %s, file: %s, size %s", c.getOriginalFileName(), c.getContentType().getMimeType(), cf, size);
            }
        } finally {
            DMSSessionFactory.getFactory().rollbackClose(session);
        }
    }
}
