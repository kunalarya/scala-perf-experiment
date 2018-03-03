from __future__ import print_function, absolute_import

import argparse
import math
import sys

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib import ticker

def visualize(df, col_count):

    # Grab all unique names.
    names = df['name'].unique().tolist()

    # Divide into columns.
    rows = math.ceil(len(names) / col_count)

    font = {'fontname': 'inconsolata for powerline'}


    fig, axes = plt.subplots(rows, col_count, figsize=(9, 3), sharex=False, sharey=True)
    xlabel = 'size of collection'
    ylabel = 'runtime (ns)'
    fig.text(0.5, 0.01, xlabel, ha='center', **font)
    fig.text(0.01, 0.5, ylabel, va='center', rotation='vertical', **font)
    
    #xticks = [2 ** x for x in range(22)]

    x_major_locator = ticker.LogLocator(base=2.0)
    y_major_locator = ticker.LogLocator(base=2.0)
            
    # For each one, create a plot
    for index, name in enumerate(names):
        data = df[df['name'] == name][['length', 'time']]
        row = index // col_count
        col = index % col_count
        if rows > 1:
            axis = axes[row][col]
        else:
            axis = axes[col]

        # Log-scale on x.
        axis.semilogx(base=2.0)
        axis.semilogy(base=2.0)
        axis.set_title(name, **font)
        axis.grid(color='#d0e1ef', linestyle='dotted', linewidth=1)
        axis.set_axisbelow(True)
        axis.scatter(data['length'], data['time'], s=8, marker='d', c='#268BD2')

    margin = 0.03
    plt.tight_layout(rect=(margin, margin, 1.0 - margin, 1.0 - margin))
    plt.show()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('filename', type=str, action='store')
    parser.add_argument('--num_cols', type=int, action='store', default=3)
    args = parser.parse_args()
    df = pd.read_csv(args.filename)
    visualize(df, args.num_cols)


if __name__ == '__main__':
    main()
