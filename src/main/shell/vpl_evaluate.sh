# Load VPL environment vars.
. common_script.sh

# Set the directory path
task_dir="task"

# Find the task file in the "task" directory
task_file=$(find "$task_dir" -type f \( -name "*.zip" -o -name "*.xml" \) -print -quit)

# Initialize the file array
file_array=()

# Read the VPL_SUBFILES variable
IFS=$'\n' read -d '' -r -a subfiles <<< "$VPL_SUBFILES"

# Iterate over the subfiles
for subfile in "${subfiles[@]}"; do
    # Initialize a flag to check if the file is in the array
    found=false
    for file in "${file_array[@]}"; do
        if [[ "$file" == "$subfile" ]]; then
            found=true
            break
        fi
    done

    # Add the unique file to the array if it wasn't found
    if [[ "$found" == false ]]; then
        file_array+=("$subfile")
    fi
done

# Create the "submission" directory if it doesn't exist
mkdir -p submission

# Iterate over the file_array and move each file to the "submission" directory
for file_path in "${file_array[@]}"; do
    # Check if the file_path contains a directory path
    if [[ "$file_path" == */* ]]; then
        # Create the directory structure within the "submission" directory
        mkdir -p "submission/$(dirname "$file_path")"
        
        # Move the file to the "submission" directory preserving the relative path
        mv "$file_path" "submission/$file_path"
    else
        # If it's just a filename, move it to the "submission" directory
        mv "$file_path" "submission/$file_path"
    fi
done

# Check Java version (must be 17 or higher)
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
java_major=$(echo "$java_version" | awk -F. '{if ($1 == "1") print $2; else print $1}')

if (( java_major < 17 )); then
    echo "Error: Java version 17 or higher is required. Found version $java_version." >&2
    exit 1
fi

if [ -f "ProformaFormatter-1.0.0-fat-jar-with-dependencies.jar" ]; then
    # Concatenate all file names into a single string separated by spaces
    file_list=$(printf "%s " "${file_array[@]}")

    if [ -n "$task_file" ]; then
        # Get the full name of the file, including its extension
        task_file_name=$(basename "$task_file")
        
        cat vpl_environment.sh >> vpl_execution
        # Call Java program and pass the concatenated file names as a single argument
        echo "java -jar ProformaFormatter-1.0.0-fat-jar-with-dependencies.jar $file_list $task_file_name" >> vpl_execution
    else
        echo "Task file missing. Task file must be either a zip or xml file. Please upload task file as follow: task/{{taskFileNameZipOrXMl}}"
        
    fi
else 
    echo "Missing file ProformaFormatter-1.0.0-fat-jar-with-dependencies.jar. Make sure the mentioned file is included in the 'Execution files' list"
fi

chmod +x vpl_execution