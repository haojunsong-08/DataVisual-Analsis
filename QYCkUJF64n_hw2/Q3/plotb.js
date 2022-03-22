//------------------------1. PREPARATION------------------------//
//-----------------------------SVG------------------------------//


//def svg
var svg_b = d3.select('body')
.append('svg')
.attr('id', 'svg-b')
.attr("preserveAspectRatio", "xMinYMin meet")
.attr("viewBox", "-"
      + adj + " -"
      + adj + " "
      + (width + adj *3) + " "
      + (height + adj*3))
.style("margin", margin)

//append char title
svg_b.append('text')
.attr('transform', 'translate('+ width/2+', -10)')
.attr("text-anchor", "middle")
.style("font-weight", "bold")
.attr("id", "title-b")
.text("Number of Ratings 2016-2020 with Rankings");

//append group based on DOM structure
svg_b.append('g')
.attr('transform', 'translate('+margin+','+margin + ")")
.attr('id', 'plot-b')

//-----------------------------DATA-----------------------------//

// get the data
var pathToCsv = 'boardgame_ratings.csv';
d3.dsv(",", pathToCsv, function (d) {
return {
// format data attributes if required
date: timeConvert(d.date) ,
count1: +d["Catan=count"],
rank1: +d["Catan=rank"],
count2: +d["Dominion=count"],
rank2: +d["Dominion=rank"],
count3: +d["Codenames=count"],
rank3: +d["Codenames=rank"],
count4: +d["Terraforming Mars=count"],
rank4: +d["Terraforming Mars=rank"],
count5: +d["Gloomhaven=count"],
rank5: +d["Gloomhaven=rank"],
count6: +d["Magic: The Gathering=count"],
rank6: +d["Magic: The Gathering=rank"],
count7: +d["Dixit=count"],
rank7: +d["Dixit=rank"],
count8: +d["Monopoly=count"],
rank8: +d["Monopoly=rank"]
}
}).then(function (data) {
console.log(data)
//the number of datapoints
//-----------------------------SCALES-----------------------------//
// create x,y ranges
let xScale = d3.scaleTime()
.range([0,width])
let yScale = d3.scaleLinear()
.range([height,0])

xScale
.domain(d3.extent(data, function(d) {return d.date; }));

yScale
.domain([0, d3.max(data, function(d) {return d.count1; })]);
//-----------------------------AXES-----------------------------//
//performed in drawing part

//----------------------------LINES-----------------------------//


const line1 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function(d) {return yScale(d.count1);})
.curve(d3.curveMonotoneX);

const line2 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function (d) {return yScale(d.count2);})
.curve(d3.curveMonotoneX);

const line3 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function (d) {return yScale(d.count3);})
.curve(d3.curveMonotoneX);

const line4 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function (d) {return yScale(d.count4);})
.curve(d3.curveMonotoneX);

const line5 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function (d){return yScale(d.count5);})
.curve(d3.curveMonotoneX);

const line6 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function (d){return yScale(d.count6);})
.curve(d3.curveMonotoneX);

const line7 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function (d){return yScale(d.count7);})
.curve(d3.curveMonotoneX);

const line8 = d3.line()
.x(function (d) { return xScale(d.date);})
.y(function (d) {return yScale(d.count8);})
.curve(d3.curveMonotoneX);

//-------------------------2. DRAWING---------------------------//
//-----------------------------AXES-----------------------------//
// add axis to svg
svg_b.select('#plot-b').append('g')
.attr('id', 'x-axis-b')
.attr("transform", "translate(0," + height + ")")
.call(d3.axisBottom(xScale).ticks().tickFormat(d3.timeFormat("%b %y")))


svg_b.select('#plot-b').append('g')
.attr('id', 'y-axis-b')
.call(d3.axisLeft(yScale));

//add text to axis
svg_b.select('#x-axis-b').append('text')
.attr("x", width/2)
.attr('y', height+50)
.style("font-weight", "bold")
.style('fill','black')
.attr("text-anchor", "middle")
.attr('id', 'x_axis_label')
.text('Month')
svg_b.select('#y-axis-b').append("text")
.attr("x", 0 - height / 2)
.attr("y", -50)
.style("font-weight", "bold")
.style('fill','black')
.attr("text-anchor", "middle")
.attr("transform", "rotate(-90)")
.attr('id', 'y_axis_label')
.text("Num of Ratings");

//----------------------------Symbols --------------------------//
symbol = svg_b.select('#plot-b').append('g')
.attr('id', 'symbols-b')


//list of games that need ranks[‘Catan’, ‘Codenames’, ‘Terraforming Mars’, ‘Gloomhaven’]
 let games_symbol = ['count1', 'count3', 'count4', 'count5']
let ranks_symbol = ['rank1', 'rank3', 'rank4', 'rank5']
let color_symbol = [color[0], color[2], color[3], color[4]]

for (var i = 0; i < games_symbol.length; i++){
      symbol.selectAll(null)
      .data(data.filter(function(d, i) {
            return i % 3 == 2
        }))
      .enter()
      .append('circle')
      .attr('fill', color_symbol[i])
      .attr('cx', function(d) { return xScale(d.date);})
      .attr('cy', function(d) { return yScale(d[games_symbol[i]]);})
      .attr('r', 12);

      //symbol label
      symbol.selectAll(null)
      .data(data.filter(function(d, i) {
            return i % 3 == 2
        }))
      .enter()
      .append('text')
      .attr("font-size", "12px")
      .attr("text-anchor", "middle")
      .attr("fill", "white")
      .attr('x', function(d) { return xScale(d.date);})
      .attr('y', function(d) { return yScale(d[games_symbol[i]]);})
      .text(function(d) { return d[ranks_symbol[i]];});
}

//----------------------------LEGENDS-------------------------------//
let legend = svg_b.select('#plot-b').append('g')
                .attr("id", "legend-b")

legend.append("circle")
      .attr("cx", width+80)
      .attr("cy", height -40)
      .attr('fill', 'black')
      .attr("r", 20)

legend.append("text")
      .attr("x", width+80)
      .attr("y", height-40)
      .attr("font-size", "12px")
      .attr("text-anchor", "middle")
      .attr("fill", "white")
      .text("Rank");

legend.append("text")
      .attr("x", width+80)
      .attr("y", height)
      .attr("font-size", "16px")
      .attr("text-anchor", "middle")
      .attr("fill", "black")
      .text("BoardGameGeek Rank");

//----------------------------LINES-----------------------------//
svg_b.select('#plot-b').append('g')
.attr('id', 'lines-b')

lines = [line1, line2, line3, line4, line5,line6, line7, line8]
counts = ['count1', 'count2', 'count3', 'count4', 'count5','count6','count7', 'count8']
games = ['Catan', 'Dominion', 'Codenames', 'Terraforming Mars', 'Gloomhaven', 'Magic: The Gathering', 'Dixit', 'Monopoly']
for (i = 0; i < lines.length; i++) {
//add lines to svg
svg_b.select('#lines-b').append('path')
.datum(data)
.style('stroke', color[i])
.style('fill', 'none')
.style('"stroke-width"', '1px')
.attr('d',lines[i]);
// add text of column to svg
svg_b.select('#lines-b')
.append("text")
.attr("transform", "translate(" + (width + 5) + "," + yScale(data[data.length - 1][counts[i]]) + ")")
.style("fill", color[i])
.text(games[i]);
}


})
