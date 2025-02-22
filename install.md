## Installation Guide for ProFormA Integration with VPL Plugin on Moodle

This guide provides step-by-step instructions for installing the ProFormA integration with the Virtual Programming Lab (VPL) plugin in Moodle. 

---

### A. Install the VPL Plugin on Moodle
1. **Introduction**: The VPL plugin enables automatic assessment of programming tasks within Moodle.
2. **Download & Install**: Refer to the [Moodle Plugin Directory](https://moodle.org/plugins/view.php?plugin=mod_vpl) for plugin download and installation instructions.

### B. Set Up the Jail-Server
1. **Purpose**: The Jail-Server is a secure sandbox environment where student code is executed and assessed.

2. **Installation Steps**:

   - Install the Jail-Server on a separate machine, virtual environment, or using VirtualBox.
   - Download the Jail-Server source files from [VPL’s official website](https://vpl.dis.ulpgc.es/).
   - Run the `install_vpl.sh` script after unzipping the files to complete the installation.

3. **Configuration**:

   - Configure the server URL in `/etc/vpl/vpl_jail_server.conf`.
   - Additional security settings, such as SSL certificates, can be configured as needed.

### C. Configure the VPL Plugin
In Moodle, navigate to **Site administration > Plugins > Plugins overview > mod_vpl > Settings** to configure the VPL plugin for communication with the Jail-Server.

1. **Add the Jail-Server URL**: Enter the Jail-Server URL you set up in the previous step into the **Execution servers list**.
2. **Disable Other Servers**: Ensure all other server options are commented out or removed.

   <img src="images/img_13.png" width="761px">


---

This installation guide provides the necessary steps to install ProFormA with VPL in Moodle. For additional details, please refer to the [thesis](https://doi.org/10.25968/opus-3176) and [official VPL documentation](https://vpl.dis.ulpgc.es/).