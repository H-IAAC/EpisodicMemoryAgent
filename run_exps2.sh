for run in 5 6 7 8 9 10
do
    echo Run ${run} / 10
    mkdir -p exps/exp2/run_${run}
    java -jar ./build/libs/CSTEpisodicMemory-full.jar -evt 0.01 -obj 0.7 -exp2 -hue 0.01 > ./exps/exp2/run_${run}/terminal.txt
    cp ./*_data ./exps/exp2/run_${run}
    mv ./exps/exp2/run_${run}/links_data ./exps/exp2/run_${run}/link_run_${run}.txt
    mv ./exps/exp2/run_${run}/object_data ./exps/exp2/run_${run}/object_run_${run}.txt
    mv ./exps/exp2/run_${run}/grid_data ./exps/exp2/run_${run}/grid_run_${run}.txt
    sleep 5
done

mkdir ./exps/exp2/all
cp ./exps/exp2/run_*/*_run* ./exps/exp2/all
mv ./exps/exp2 ./exps/exp_+rgb_100_semID_hue001
