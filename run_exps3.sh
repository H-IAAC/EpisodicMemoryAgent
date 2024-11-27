for run in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
do
    echo Run ${run} / 15
    mkdir -p exps/exp3/run_${run}
    java -jar ./build/libs/CSTEpisodicMemory-full.jar -evt 0.01 -obj 0.5 -exp3 -hue 0.01 > ./exps/exp3/run_${run}/terminal.txt
    cp ./*_data ./exps/exp3/run_${run}
    mv ./exps/exp3/run_${run}/links_data ./exps/exp3/run_${run}/link_run_${run}.txt
    mv ./exps/exp3/run_${run}/object_data ./exps/exp3/run_${run}/object_run_${run}.txt
    mv ./exps/exp3/run_${run}/grid_data ./exps/exp3/run_${run}/grid_run_${run}.txt
    sleep 5
done

mkdir ./exps/exp3/all
cp ./exps/exp3/run_*/*_run* ./exps/exp3/all
