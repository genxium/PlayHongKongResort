if [ $# -ne 3 ]; then
	echo "Usage: sh recursive_replacement.sh <root dir path> <keyword to be replaced> <replacement>\n"
	exit 1
fi
path=$1
foo=$2
bar=$3
# use double quote for sed to take bash variables
find $1 -type f -not -name "*.sh" -exec sed -i.sedbak "s/$foo/$bar/g" {} + 
find $1 -type f -name "*.sedbak" -delete
