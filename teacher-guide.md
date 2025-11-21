# Teacher usage Guide for ProFormA Integration with the VPL Plugin on Moodle

This guide provides step-by-step instructions for using the ProFormA integration with the Virtual Programming Lab (VPL) plugin in Moodle. It explains how to set up a sample VPL activity that connects to a ProFormA compatible grader to automatically assess student's submissions. The tutorial covers both the extended VPL plugin - which simplifies the setup by automating most tasks - and the standard (vanilla) VPL plugin. For further details, refer to the [official VPl documentation](https://vpl.dis.ulpgc.es/) and [this thesis](https://doi.org/10.25968/opus-3176).

---

# Option 1: Using the extended VPL plugin

This version of the VPL plugin drastically simplifies the process of setting up ProFormA-based activities by automating most of the teacher's tasks. You will learn how to create a VPL activity, upload the ProFormA-task-file and configure the grader settings so that student's submissions are automatically assessed by ProFormA compatible graders.

## Part A: Setting up the VPL activity

1. **Create a New VPL Activity**:

   - In Moodle, go to your course and create a new VPL activity. The name and description will be automatically extracted from the ProFormA task file. For now, you can give the activity any name.

2. **Open ProFormA task settings through Execution options**:

   - Inside the new VPL Activity, naviate to the "Execution options" tab.
   - Right above the "Save options" button, click on "Open ProFormA task settings page".

   ![VPL Execution options](./images/VPL_Execution_options.png)

3. **Follow further instructions displayed on the page**

   - On the ProFormA task settings page, there will be a section called "Teacher guide", which contains all necessary information to complete the setup.

4. **Done**

   - After following the displayed steps, the grading of the VPL activity is achieved using a ProFormA compatible grader.

## Part B: Change an existing VPL activity

If you have already set up a VPL activity using the ProFormA task settings page, but you want to cange either the task file or the version of the used VPL-ProFormA-Integration, you can do that in two different ways.

- **Re-submit the ProFormA task settings page**

   - You can just go back to the ProFormA task settings page as explained in Part A and re-submit it. The current task file and VPL-ProFormA-Integration will be replaced with the new ones. Although this is the easier option to change an existing VPL activity, you need to be aware of two edge cases:
      - If there is already an existing *proforma_settings.sh* file in the Execution files tab, it will **not** be replaced when re-submitting, as it probably already contains the information necessary to connect to a ProFormA backend. If you need a fresh *proforma_settings.sh* file, you need to delete it beforehand in the Execution files tab.
      - If you change the current version of the VPL-ProFormA-Integration to another one, be aware that the new *proforma_settings.sh* requires different fields as the currently used one. If you intend to change the version of the VPL-ProFormA-Integration, it is better to delete the *proforma_settings.sh* and re-type it's contents.

- **Manually adjust the Execution files**

   - You can manually change the files in the Execution files tab for altering an existing activity. The important files are the *vpl_evaluate.sh*, the task file which needs to be placed in a *task* directory, the *ProformaFormatter.jar* file and the *proforma_settings.sh* file. Please refer to Option 2 of this documentation.


# Option 2: Using the standard VPL plugin

This option uses the original VPL plugin without any extensions. This setup is more complicated and requires more manual work than Option 1.

## Part A: Configuring and Using VPL/ProFormA Activities

### Create and Configure a VPL Activity

1. **Create a new VPL Activity**
   
   - In Moodle, go to your course and create a new VPL activity. Name and describe the activity for clarity.
   - If students need to submit multiple files, set "Maximum number of files" to a number greater than 1 under "Submission restrictions".

   ![VPL Submission Restrictions](./images/VPL_Submission_Restrictions.png)

2. **Configure Execution Settings**

   - Go to "Execution options" and enable the "Run", "Debug", and "Evaluate" settings to allow students to execute and submit code.

   ![VPL vanilla Execution options](./images/VPL_Execution_options_vanilla.png)

3. **Optional Configuration**

   - Set a "Based on" activity for this activity. See the [Based-On Feature](#c-using-the-based-on-feature-to-simplify-activity-setup) section for details.
   - Enable "Evaluate just on submission" and "Automatic grade". Refer to the [Execution Options Configuration](https://vpl.dis.ulpgc.es/documentation/vpl-3.4.3+/basicfeatures.html#selecting-programming-language-tools) for more information.
   - Under "More/Maximum execution resources limits", configure the resource limits per student submissions if needed.

   ![VPL Resource Limits](./images/VPL_Resource_Limits.png)

   - In "Requested files", specify files that will be visible to students during the task (e.g., library files or starter code).Refer to the [Execution Options Configuration](https://vpl.dis.ulpgc.es/documentation/vpl-3.4.3+/basicfeatures.html#requested-files) for more information.

   ![VPL Requested files](./images/VPL_Requested_files.png)

### Upload Execution Files and Enable ProFormA Integration

Navigate to "More/Execution files" to upload the necessary files

#### Tips for Uploading

- **Drag-and-Drop**: Use the drag-and-drop feature to upload the ZIP file directly into the VPL interface. This method is faster than navigating through the file system.   
- **Import**: Expand the tool bar with the "+" symbol and then click the Import symbol (upwards arrow). Or try the keyboard shortcut `Ctrl + I`.
- **ZIP Files**:  
When working with ZIP files in VPL, keep the following in mind:  
   - **Automatic Extraction**:  
   VPL automatically extracts the contents of uploaded ZIP files.  
   - **Encoding Limitation**:  
   VPL only supports ZIP files with CP437-encoded filenames. Files with UTF-8 encoded filenames will be rejected.  
   - **Relevance of Double-Zipping**:  
   Since double-zipping is already required for the ProFormA task file, this process inherently avoids the encoding issue. The inner ZIP file containing the task files remains unexposed to VPL’s encoding restrictions. Refer to point 4 in the [Files to be uploaded](#files-to-be-uploaded) for more details.
- **Save Changes**: Always save your changes after uploading by pressing `Ctrl + S` or clicking the "Save" button in the interface.

#### Files to be uploaded

1. **Replace `vpl_evaluate.sh`**:
   
   Upload the customized `vpl_evaluate.sh` script in the "Execution files" tab to replace the default VPL script. This script enables ProFormA-based grading. 

   ![Screenshot vpl_evaluate.sh](./images/Screenshot_vpl_evaluate.sh.png)

2. **Add `proforma_settings.sh`**:

   Upload the `proforma_settings.sh` script in the same "Execution files" tab. This file lets instructors configure the grader (e.g., Graja, GraFlap) and set grading parameters.

   ![Screenshot proforma_settings.sh](./images/Screenshot_proforma_settings.sh.png)

3. **Upload the ProFormAFormatter Fat Jar**:

   Upload the `ProformaFormatter-<version>-fat-jar-with-dependencies.jar` directly into the "Execution files" tab. This file serves as a bridge between VPL and Grappa, enabling ProFormA-based communication with graders.

   ![Screenshot ProFormA Fat Jar](./images/Screenshot_ProFormAFormatter_fat_jar.png)

4. **Upload the ProFormA Task File**:

   In the "Execution files" tab, the ProFormA task file must be uploaded under a folder named `task` for the program to function correctly. Since VPL unzips uploaded ZIP files automatically, you should follow these steps based on the type of file you are uploading:

   a. **If the task file is a ProFormA ZIP file** (e.g., `helloworld.zip`):

      - Within your file system, locate the ZIP file containing the ProFormA task file (e.g., `helloworld.zip`).
      - Place this ZIP file inside a folder named `task`.
      - Zip the entire `task` folder. The resulting structure should look like this:
        ```
        task
        ├── helloworld.zip
        ```
      - Upload this outer ZIP file. VPL will unzip it, creating the required directory structure with the ProFormA task file inside the `task` folder.\
      
         ![Screenshot ProFormA task file](./images/Screenshot_proforma_task_file.png)

   b. **If the task file is an XML file** (e.g., `helloworld.xml`):
      - Place the `helloworld.xml` file directly into a folder named `task`.
      - Zip the entire `task` folder. The resulting structure should look like this:
        ```
        task
        ├── helloworld.xml
        ```
      - Upload this outer ZIP file. VPL will unzip it, creating the required directory structure with the `helloworld.xml` file inside the `task` folder.

5. **Mark Files to Keep When Running**:
   
   Mark `proforma_settings.sh`, the `ProformaFormatter-<version>-fat-jar-with-dependencies.jar`, and the ProFormA task file in the `task` folder as "Files to Keep When Running" to prevent them from being deleted after execution.

   ![Screenshot Files to keep when running](./images/Screenshot_Files_to_keep_when_running.png)

### Using the Based-On Feature to Simplify Activity Setup

The "Based-On" feature in VPL allows you to create a generic "Base Activity" that serves as a template for other VPL activities, streamlining setup. A detailed example is described in the section [Based-On Example](#example-e2-using-a-based-on-base-activity-for-new-activities).

1. **Create a Base Activity**:

   Set up a VPL activity in Moodle with essential configurations, including execution scripts, grading settings, and "Files to Keep When Running" (such as `proforma_settings.sh` and the `ProformaFormatter-<version>-fat-jar-with-dependencies.jar`).

2. **Execution Files Inheritance**:

   Execution files in the base activity will automatically apply to new activities created with the "Based-on" feature. Custom scripts like `vpl_evaluate.sh` will be appended from the base if the current activity contains its own version.

3. **Configure Expiration and Hide Base Activity**:

   - Set the base activity with no due date to ensure it's available for the entire course duration.

     ![Screenshot Submission period](./images/Screenshot_Submission_period.png)

   - Hide the base activity from students to avoid confusion by setting its visibility to "Hidden".

     ![Screenshot hidden](./images/Screenshot_hidden.png)

4. **Choose How to Use the Base Activity**:

   - When `proforma_settings.sh` is included in the base activity's "Execution files", all inheriting activities will share this configuration. To specify a different grader or settings, override the base `proforma_settings.sh` file by adding a new one in the inheriting activity.
   - Alternatively, you can create multiple base activities, each configured for a different ProFormA grader.
   - It is also possible to layer base activities. For example, create a primary base activity that defines core execution scripts, then add secondary base activities for specific `proforma_settings.sh` configurations, allowing flexible inheritance across multiple activities.

## Part B: Example Configurations

### Example E1: Creating a VPL Activity with ProFormA Integration from Scratch

This example demonstrates how to create a basic new VPL activity and integrate it with ProFormA and the the Graders.

1. **Basic Setup**:
   - Create a new VPL activity in your course.
   - Set the name and description.
   - Configure the "Maximum number of files" setting as required.
2. **Upload ProFormA Integration Files**:
   - In "Execution files", upload `vpl_evaluate.sh`, `proforma_settings.sh`, and the `ProformaFormatter-<version>-fat-jar-with-dependencies.jar` directly.
   - Create a folder named `task` in "Execution files" and upload the ProFormA task file into this `task` folder.
3. **Mark Files to Keep**:
   Go to "More/Files to keep when running" and mark `proforma_settings.sh`, the `ProformaFormatter-<version>-fat-jar-with-dependencies.jar`, and the ProFormA task file within the `task` folder.
4. **Finalize Activity Configuration**:
   Under "Execution options", set "Run", "Debug", and "Evaluate" to "Yes" to enable these features for students.
5. **Testing**:
   Go to "Virtual programming lab/Test activity/Edit" and run a test evaluation to verify ProFormA integration.

### Example E2: Using a Based-On Base Activity for New Activities

This example illustrates how to use the Based-On feature with a base activity.

1. **Set Up the Base Activity**:
   - Create a VPL activity that will serve as a base for other VPL activities.
   - Under "Settings", remove the "due date" to keep it accessible throughout the entirety of hte course duration.
   - Hide the activity from students to maintain a clean course structure.
   - Upload the execution scripts (custom `vpl_evaluate.sh` script and `proforma_settings.sh` script) and the `ProformaFormatter-<version>-fat-jar-with-dependencies.jar` to "Execution files".
   - In "Execution options", set "Run", "Debug", and "Evaluate" to "Yes".
   - Go to "More/Files to Keep When Running" and check the boxes for `proforma_settings.sh` and the `ProformaFormatter-<version>-fat-jar-with-dependencies.jar`.
2. **Create a New Activity Based on the Base Activity**:
   - Create a new VPL activity in your course.
   - In "Execution options", select the base activity from the previous step in the "Based-on" dropdown.
   - This new activity will inherit all configurations from the base, including execution files and grading settings.
   - Under "More/Execution files", upload the ProFormA task file in a `task` folder.
   - Under "Files to Keep When Running", mark the uploaded ProFormA task file to prevent deletion.
3. **Testing**:
   Go to "Virtual programming lab/Test activity/Edit" and run a test evaluation to verify ProFormA integration.
