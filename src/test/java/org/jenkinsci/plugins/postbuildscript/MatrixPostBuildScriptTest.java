package org.jenkinsci.plugins.postbuildscript;

import com.thoughtworks.xstream.XStream;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.model.ExecuteOn;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.jenkinsci.plugins.postbuildscript.model.ScriptType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MatrixPostBuildScriptTest {

    @Mock
    private Script script;

    @Mock
    private ScriptFile genericScriptFile;

    @Mock
    private ScriptFile groovyScriptFile;

    @Mock
    private PostBuildStep postBuildStep;

    @Mock
    private MatrixBuild matrixBuild;

    @Mock
    private Launcher launcher;

    @Mock
    private BuildListener listener;

    private MatrixPostBuildScript resolvedMatrixPostBuildScript;

    private MatrixPostBuildScript matrixPostBuildScript;
    private MatrixPostBuildScript.DescriptorImpl descriptor;

    @Before
    public void setUp() {

        given(genericScriptFile.getScriptType()).willReturn(ScriptType.GENERIC);
        given(groovyScriptFile.getScriptType()).willReturn(ScriptType.GROOVY);

    }

    @Test
    public void keepsPostBuildItems() {

        givenMatrixPostBuildScript();

        assertThat(matrixPostBuildScript.getGenericScriptFiles(), contains(genericScriptFile));
        assertThat(matrixPostBuildScript.getGroovyScriptFiles(), contains(groovyScriptFile));
        assertThat(matrixPostBuildScript.getGroovyScripts(), contains(script));
        assertThat(matrixPostBuildScript.getBuildSteps(), contains(postBuildStep));
        assertThat(matrixPostBuildScript.isMarkBuildUnstable(), is(true));

        verify(genericScriptFile).setScriptType(ScriptType.GENERIC);
        verify(groovyScriptFile).setScriptType(ScriptType.GROOVY);

    }

    @Test
    public void createsAggregator() {

        givenMatrixPostBuildScript();

        MatrixAggregator aggregator = matrixPostBuildScript.createAggregator(matrixBuild, launcher, listener);

        assertThat(aggregator, is(notNullValue()));

    }

    @Test
    public void readResolveAppliesExecuteOnOnEachItem() {

        givenScriptFromConfig("/v0.18_config_matrix.xml");

        whenReadResolves();

        PostBuildStep postBuildStep = resolvedMatrixPostBuildScript.getBuildSteps().get(0);
        assertThat(postBuildStep.getExecuteOn(), is(ExecuteOn.MATRIX));

    }

    @Test
    public void containsHelpFile() {

        givenDescriptor();

        assertThat(descriptor.getHelpFile(), is("/plugin/postbuildscript/help/postbuildscript.html"));

    }

    @Test
    public void appliesToMatrixJob() {

        givenDescriptor();

        Class<? extends AbstractProject> jobType = MatrixProject.class;
        assertThat(descriptor.isApplicable(jobType), is(true));

    }

    private void givenMatrixPostBuildScript() {
        matrixPostBuildScript = new MatrixPostBuildScript(
            Collections.singleton(genericScriptFile),
            Collections.singleton(groovyScriptFile),
            Collections.singleton(script),
            Collections.singleton(postBuildStep),
            true
        );
    }

    private void givenScriptFromConfig(String configResourceName) {
        XStream xstream = new XStream();
        matrixPostBuildScript = (MatrixPostBuildScript) xstream.fromXML(getClass().getResource(configResourceName));
    }


    private void givenDescriptor() {
        descriptor = new MatrixPostBuildScript.DescriptorImpl();
    }

    private void whenReadResolves() {
        resolvedMatrixPostBuildScript = (MatrixPostBuildScript) matrixPostBuildScript.readResolve();
    }

}
