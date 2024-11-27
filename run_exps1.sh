for evt in 0.25 #0.5 0.2 0.1 0.01 0.001 0.0001
do
    for obj in 1.0 0.99 0.95 0.9 0.8 0.7 0.6 0.5 0.4 0.3 0.2 0.1 0.0
    do
        echo Run ${evt} - ${obj}
        mkdir -p exps/exp1/run_${evt}_${obj}
        java -jar ./build/libs/CSTEpisodicMemory-full.jar -evt $evt -obj $obj -hue 0.05 -exp1 > ./exps/exp1/run_${evt}_${obj}/terminal.txt
        cp ./*_data ./exps/exp1/run_${evt}_${obj}
        mv ./exps/exp1/run_${evt}_${obj}/links_data ./exps/exp1/run_${evt}_${obj}/link_run_${evt}_${obj}.txt
        mv ./exps/exp1/run_${evt}_${obj}/object_data ./exps/exp1/run_${evt}_${obj}/object_run_${evt}_${obj}.txt
        mv ./exps/exp1/run_${evt}_${obj}/grid_data ./exps/exp1/run_${evt}_${obj}/grid_run_${evt}_${obj}.txt
    done
done
mkdir ./exps/exp1/all
cp ./exps/exp1/run_*/*_run* ./exps/exp1/all

