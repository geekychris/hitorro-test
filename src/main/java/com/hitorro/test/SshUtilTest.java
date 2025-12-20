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
package com.hitorro.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.basedms.ssh.SshCommand;
import com.hitorro.basedms.ssh.SshConstants;
import com.hitorro.basedms.ssh.SshExec;
import com.hitorro.basedms.ssh.SshSession;
import com.hitorro.basedms.ssh.scp.ScpFileStatus;
import com.hitorro.basedms.ssh.scp.ScpFrom;
import com.hitorro.basedms.ssh.scp.ScpTo;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.util.HashMap;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: rmonaghan Date: Dec 3, 2006 Time: 3:28:47 PM
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Test SSH secure utility library.")
public class SshUtilTest extends EnhancedTestCase {

    private String PROPERTY_KEY_ROOT = this.getClass().getName();
    private JsonNode _testProperties;


    /*   define keys   */
    private StringProperty _hostKey = new StringProperty("host", "production host", null);
    private StringProperty _userKey = new StringProperty("user", "production host user", null);
    private StringProperty _passwordKey = new StringProperty("password", "production host user", null);
    private IntegerProperty _portKey = new IntegerProperty("port", "production host port", 22);
    private BooleanProperty _trustKey = new BooleanProperty("trust", "production ssh trust relationship", false);

    /*   declare key values   */
    private String _host;
    private String _user;
    private String _password;
    private int _port;
    private boolean _trust;

    protected void setUp() throws Exception {

        super.setUp();

        /*   get key values.   */
        addPropsFromFile(getInputFileRelative("SshUtilTest.properties", true));
        _testProperties = JVSProperties.getProperties().get(PROPERTY_KEY_ROOT);


        _user = _userKey.apply(_testProperties);
        _password = _passwordKey.apply(_testProperties);
        _host = _hostKey.apply(_testProperties);
        _port = _portKey.apply(_testProperties);
        _trust = _trustKey.apply(_testProperties);

        String testOutputDir = getOutputFileRelative("", true).toString();
        File testOutputDirFile = new File(testOutputDir);

        /*   basestate clean-up   */
        if (testOutputDirFile.exists()) {
            FileUtil.removeDirectory(testOutputDirFile);
        }

    }


    protected void tearDown() throws Exception {
        super.tearDown();
        reloadConfig();

    }

    public void testScpTo() {
        String filenotexists = "filenotexists.txt";
        String targetPathname = "/tmp";
        String sourcePathRoot = getInputFileRelative("", true).toString();
        String[] sourceFilePathnames = {StringUtil.strcat(sourcePathRoot, "/data/Poe*.*"),                      /*   wildcarded files      */
                StringUtil.strcat(sourcePathRoot, "/Shakespeare, William Macbeth.txt"), /*   specific file         */
                StringUtil.strcat(sourcePathRoot, "/", filenotexists)};                 /*   file does not exist   */

        /*   chunkParser testcase.   */
        SshSession sshSession = SshSession.getSshSession(_user, _password, _host, _port, _trust);
        ScpTo scpto = ScpTo.getScpTo(sshSession, sourceFilePathnames, targetPathname);
        HashMap<String, ScpFileStatus> files = scpto.execute();

        /**
         *   verify results.  assume that all files have been found; all files are transferred, except for filenotexists file.
         *   todo: chris: put checks on the files transferred and the content transferred.
         */
        for (String key : files.keySet()) {
            ScpFileStatus fileStatus = files.get(key);
            int xferStatus = fileStatus.getFileTransferStatus();

            if (key.equalsIgnoreCase(StringUtil.strcat(sourcePathRoot, File.separator, filenotexists))) {
                assertEquals(xferStatus, SshConstants.SSH_FILEXFER_FILENOTFOUND);
            } else {
                assertEquals(xferStatus, SshConstants.SSH_FILEXFER_OK);
            }
        }
    }


    public void testScpFrom() {
        String filenotexists = "filenotexists.txt";
        String targetPathname = getOutputFileRelative("", true).toString();
        String sourcePathRoot = "/tmp";
        String[] sourceFilePathnames = {StringUtil.strcat(sourcePathRoot, "/Poe*.*"),                               /*   wildcarded files      */
                StringUtil.strcat(sourcePathRoot, "/Shakespeare, William Macbeth.txt"),     /*   specific file         */
                StringUtil.strcat(sourcePathRoot, File.separator, filenotexists)};          /*   file does not exist   */

        /*   chunkParser dependent testcase (for testcase data setup)  */
        //   todo:  chris: we need a way to track testcase dependencies.  junit provides nothing ootb.
        this.testScpTo();

        /*   chunkParser testcase.   */
        SshSession sshSession = SshSession.getSshSession(_user, _password, _host, _port, _trust);
        ScpFrom scpfrom = ScpFrom.getScpFrom(sshSession, sourceFilePathnames, targetPathname);
        HashMap<String, ScpFileStatus> files = scpfrom.execute();

        /**
         *  verify results.  assume that all files have been found; all files are transferred, except for filenotexists file.
         */
        for (String key : files.keySet()) {
            ScpFileStatus fileStatus = files.get(key);
            int xferStatus = fileStatus.getFileTransferStatus();

            if (key.equalsIgnoreCase(StringUtil.strcat(sourcePathRoot, "/", filenotexists))) {
                assertEquals(xferStatus, SshConstants.SSH_FILEXFER_FILENOTFOUND);
            } else {
                assertEquals(xferStatus, SshConstants.SSH_FILEXFER_OK);
            }
        }
    }

    public void testSshExec() {
        String results[];
        String result;
        int exitCode;

        String[] files = {"/tmp/SshUtilTest_testSshExec_0.txt",
                "/tmp/SshUtilTest_testSshExec_1.txt"};


        SshSession sshSession = SshSession.getSshSession(_user, _password, _host, _port, _trust);
        SshExec sshExec = SshExec.getSshExec(sshSession);
        SshCommand sshCmd = SshCommand.getSshCommand(sshSession);

        /*   create files on remote host via SshExec.   */
        result = sshExec.execute(sshExec.getStatusCommand("ls -al /tmp > /tmp/SshUtilTest_testSshExec_0.txt; ls -al /tmp > /tmp/SshUtilTest_testSshExec_1.txt"));
        exitCode = sshExec.getExitStatus(result);
        assertEquals("Verify files created on remote host via SshExec.", SshConstants.SSH_SHELLSTATUS_OK, exitCode);

        /*   verify remote files exist via SshExec.chunkParser().  */
        result = sshExec.execute("ls -al /tmp/SshUtilTest_testSshExec_1.txt");
        int fileIndex = result.indexOf("SshUtilTest_testSshExec_1.txt");
        int fileMaskIndex = result.indexOf("-rw-r");
        assertTrue("Verify files exist on remote host via SshExec 'ls'.", fileIndex >= 0 && fileMaskIndex >= 0);

        /*   verify remote files exist via SshCommand.ls().  */
        results = sshCmd.ls("/tmp/SshUtilTest_testSshExec_*.txt");
        assertTrue("Verify files exist on remote host via SshCommand 'ls'.", results.length == 2);

        /*    delete just-created remote files via SshCommand.  */
        for (String file : results) {
            exitCode = sshCmd.rm(file);
            assertEquals("Verify files are deleted on remote host vis SshCommand 'rm'", SshConstants.SSH_SHELLSTATUS_OK, exitCode);

        }

        /*   verify files do not exist via SshCommand.  */
        for (String file : files) {
            results = sshCmd.ls(file);
            assertNull("Verify files were deleted on remote host vis SshCommand 'ls'", results);
        }
    }

}
