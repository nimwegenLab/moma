#!/bin/bash

session_type=$1
session_name=$2


if [[ ! $session_type ]]; then
  printf "ERROR: No session type proved.\n"
  exit 1
fi

if [[ ! $session_name ]]; then
  printf "ERROR: No session branch name provided.\n"
  exit 1
fi

allowed_session_types=("feature" "bugfix" "exploration")

if [[ ! " ${allowed_session_types[*]} " =~ " ${session_type} " ]]; then
  printf "ERROR: The provided session type \"%s\" is invalid. Allowed values are: ${allowed_session_types[*]}\n" "${session_type}"
  exit 1
fi

if [[ "$session_name" =~ \/ ]]; then
  printf "ERROR: The provided session branch \"%s\" is invalid. The branch name should not contain a slash (i.e. '/') or sub-branch (I will add it for you).\n" "$session_name"
  exit 1
fi

full_topic_branch_name="$session_type"/"$session_name"

printf "Session type: %s\n" "$session_type"
printf "Session branch: %s\n" "$full_topic_branch_name"

# TODO: Uncomment this, when development has finished!
#if [[ $(git diff --stat) != '' ]]; then
#  printf 'Cannot start a new session: The working tree is dirty. Please commit changes first.\n'
#  exit 1
#fi

git branch "$full_topic_branch_name"
git checkout "$full_topic_branch_name"

test_folder_path="/home/micha/Documents/01_work/git/MoMA/src/test/java/com/jug"
devel_data_folder="/home/micha/Documents/01_work/15_moma_notes/02_moma_development"
topic_data_template_folder="$devel_data_folder/00_test_datasets/gl_data_1_template"
class_folder="/home/micha/Documents/01_work/git/MoMA/src/test/java/com/jug/INTERACTIVE_TESTS_TEMPLATE.java"

printf "Starting debug session for branch:\n\t%s\n" "$FULL_BRANCH_NAME"

BUGFIX_BRANCH_NAME="${FULL_BRANCH_NAME/bugfix\//}"
topic_branch_test_class="Bugfix__${BUGFIX_BRANCH_NAME//-/_}" # this replaces occurences of "-" with "_"
topic_branch_data_folder="$devel_data_folder"/"$session_type"/"$session_name"

echo "$topic_branch_data_folder"

mkdir -p "$topic_branch_data_folder"

cp -P "$topic_data_template_folder"/* "$topic_branch_data_folder"
#cp "$class_folder/Bugfix__TEMPLATE.java" "$class_folder/$topic_branch_test_class.java"
#sed -i "s/Bugfix__TEMPLATE/$topic_branch_test_class/g" "$class_folder/$topic_branch_test_class.java"
#sed -i "s/000__debug_template/$BUGFIX_BRANCH_NAME/g" "$class_folder/$topic_branch_test_class.java"
#git add "$class_folder/$topic_branch_test_class.java"

exit

printf "The data folder for this debug session is:\n\t%s\n" "$topic_branch_data_folder"
printf "The class for this debug session is:\n\t%s\n" "$topic_branch_test_class"

git checkout "feature/20221013-add-script-to-generate-a-topic-session"  # TODO: Remove this, when development has finished!