package org.openmrs.module.conceptpropose.functionaltest.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openmrs.module.conceptpropose.pagemodel.AdminPage;
import org.openmrs.module.conceptpropose.pagemodel.CreateProposalPage;
import org.openmrs.module.conceptpropose.pagemodel.ManageProposalsPage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ProposalStepDefs {
    private CreateProposalPage createProposalPage;
    private ManageProposalsPage manageProposalsPage;
    private AdminPage adminPage;


    private String generatedName = "";
    private int oldConceptCount = 0;
    private String generatedComment = "";

    @Given("^I have a saved draft proposal$")
    public void navigate_to_page() throws IOException, InterruptedException {
        login();
        loadNewProposalPage();
        adminPage.navigateToAdminPage();
        loadManageProposalsPage();
        manageProposalsPage.navigateToDraftProposal("");
    }

    @Given("^I have a saved draft proposal with zero concepts")
    public void saved_draft_proposal_with_zero_concepts() throws IOException, InterruptedException {
        login();
        loadNewProposalPage();
        adminPage.navigateToAdminPage();
        loadManageProposalsPage();
        manageProposalsPage.navigateToDraftProposalWithNoConcepts("");
    }

    @Given("^I have a saved draft proposal with at least 1 concept")
    public void saved_draft_proposal_with_at_least_1_concept() throws IOException, InterruptedException {
        login();
        loadNewProposalPage();
        adminPage.navigateToAdminPage();
        loadManageProposalsPage();
        // find proposal with more than 1
        manageProposalsPage.navigateToDraftProposalWithConcepts("");
    }

    @Given("^I have a saved draft proposal for deletion")
    public void navigate_to_proposal_for_deletion() throws IOException, InterruptedException {
        loadManageProposalsPage();
        System.out.println("Delete " + generatedName);
        manageProposalsPage.navigateToDraftProposal(generatedName);
    }

    @When("^I submit the proposal$")
    public void submit_proposal() throws IOException, InterruptedException {
        createProposalPage.submitProposal();
    }

    @Then("^the proposal is sent to the dictionary manager$")
    public void check_the_dictionary_manager() throws IOException, InterruptedException{
        loadManageProposalsPage();
        assertThat(manageProposalsPage.findProposalStatus(generatedName), equalTo("Submitted"));
    }

    @When("^I change the details and save$")
    public void edit_existing_proposal() throws IOException, InterruptedException {
        generatedName = newRandomString();
        System.out.println("Generated email " + generatedName);
        createProposalPage.enterNewProposal(generatedName, "email_edit@example.com", "Some Comments Edit");
        manageProposalsPage = createProposalPage.editExistingProposal();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    @When("I add a concept and save")
    public void add_concept_to_draft_proposal(){
        oldConceptCount = createProposalPage.getNumberOfConcepts();
        System.out.println("Old concept count: " + oldConceptCount);
        generatedName = newRandomString();
        System.out.println("New name: " + generatedName);
        createProposalPage.enterNewProposal(generatedName, "email_edit@example.com", "Some Comments Edit");
        createProposalPage.navigateToAddConceptDialog();
        // TODO: need to ensure that this hasn't been previously entered
        createProposalPage.enterNewConcept("ba", 1);
        createProposalPage.enterNewConceptComment("This is ab");
        createProposalPage.editExistingProposal();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    @Then("the proposal is stored with the added concept details")
    public void check_added_concept_details() throws IOException{
        loadManageProposalsPage();
        assertThat(manageProposalsPage.getProposalConceptCount(generatedName), equalTo(oldConceptCount+1));
    }

    @When("I change the first concept comment")
    public void edit_concept(){
        generatedName = newRandomString();
        createProposalPage.enterNewProposal(generatedName, "email_edit@example.com", "Some Comments Edit");
        generatedComment = newRandomString();
        createProposalPage.enterNewConceptComment(generatedComment);
        createProposalPage.editExistingProposal();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    @Then("the concept comment is saved")
    public void check_edited_concept_details(){
        manageProposalsPage.navigateToDraftProposal(generatedName);
        assertThat(createProposalPage.getConceptComment(), equalTo(generatedComment));
    }

    @Then("^the proposal is stored with the new details$")
    public void check_the_edited_details() throws InterruptedException, IOException {
        loadManageProposalsPage();
        manageProposalsPage.navigateToDraftProposal(generatedName);
        // TODO details of concepts added?
        assertThat(createProposalPage.getName(), equalTo(generatedName));
        assertThat(createProposalPage.getComment(), equalTo("Some Comments Edit"));
        assertThat(createProposalPage.getEmail(), equalTo("email_edit@example.com"));
    }

    @Given("^I have a new proposal with all necessary details$")
    public void fill_a_new_proposal() throws IOException {
        login();
        loadNewProposalPage();
        createProposalPage.enterNewProposal("Some Name", "email@example.com", "Some Comments");
    }

    @When("^I save$")
    public void save_new_proposal(){
        manageProposalsPage = createProposalPage.saveNewProposal();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    @Then("^the proposal is stored with the details$")
    public void check_the_details() throws InterruptedException, IOException {
        // previous step must wait for the page to be 'fully loaded' before calling this step
        // as loadProposalMonitorPage() looks for 'monitor proposals' link which could be on the 'previous' page
        // causing a stale element exception will occur (e.g. on the create proposal page and clicking the save button)
        loadManageProposalsPage();
        // TODO: need to verify email, concept added and concept comment
        assertThat(manageProposalsPage.getLastProposalName(), equalTo("Some Name"));
        assertThat(manageProposalsPage.getLastProposalDescription(), equalTo("Some Comments"));
        // assertThat(manageProposalsPage.getConceptCount(), equalTo("1"));
    }

    @When("^I delete a concept")
    public void delete_concept() {
        oldConceptCount = createProposalPage.getNumberOfConcepts();
        generatedName = newRandomString();
        createProposalPage.enterNewProposal(generatedName, "email_edit@example.com", "Some Edit Comment");
        createProposalPage.deleteExistingConcept();
        createProposalPage.saveNewProposal();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    @When("^I start to delete a concept then cancel")
    public void start_delete_concept() {
        oldConceptCount = createProposalPage.getNumberOfConcepts();
        generatedName = newRandomString();
        createProposalPage.enterNewProposal(generatedName, "email_edit@example.com", "Some Edit Comment");
        createProposalPage.startDeleteExistingConcept();
        createProposalPage.cancelAtConfirmationPrompt();
        createProposalPage.saveNewProposal();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    @Then("^the concept is deleted")
    public void concept_is_deleted() throws IOException {
        loadManageProposalsPage();
        manageProposalsPage.navigateToDraftProposal(generatedName);
        assertThat(createProposalPage.getNumberOfConcepts(), equalTo((oldConceptCount-1)));
    }

    @Then("^the concept still exists in the proposal")
    public void concept_still_exists() throws IOException {
        loadManageProposalsPage();
        manageProposalsPage.navigateToDraftProposal(generatedName);
        assertThat(createProposalPage.getNumberOfConcepts(), equalTo((oldConceptCount)));
    }

    @When("I delete the proposal")
    public void delete_proposal() {
        createProposalPage.deleteProposal();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    @When("I start to delete the proposal then cancel")
    public void start_delete_proposal_then_cancel() {
        createProposalPage.startDeleteProposal();
        createProposalPage.cancelAtConfirmationPrompt();
    }

    @Then("the proposal is deleted")
    public void proposal_is_deleted() throws IOException{
        loadManageProposalsPage();
        assertNull(manageProposalsPage.findDraftProposalByName(generatedName));
    }

    @Then("the proposal still exists")
    public void proposal_still_exists() throws IOException{
        loadManageProposalsPage();
        assertNotNull(manageProposalsPage.findDraftProposalByName(generatedName));
    }

    private void loadManageProposalsPage() throws IOException {
        manageProposalsPage = adminPage.navigateToManageProposals();
        manageProposalsPage.waitUntilFullyLoaded();
    }

    private void loadNewProposalPage() throws IOException {
        createProposalPage= adminPage.navigateToCreateProposalPage();
    }
    private void login()  throws IOException{
        Login login = new Login();
        adminPage = login.login();
    }

    private String newRandomString(){
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }
}
