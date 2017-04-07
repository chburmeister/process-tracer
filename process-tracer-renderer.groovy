@Grapes([
 @Grab(group='org.xerial',module='sqlite-jdbc',version='3.16.1'),
 @GrabConfig(systemClassLoader=true)
])
import groovy.sql.Sql
def sql = Sql.newInstance("jdbc:sqlite:process-tracer.db", "org.sqlite.JDBC")

def traceFileDirectoryName="traces";
new File(traceFileDirectoryName).mkdirs()

def processCorelationIds = []


sql.eachRow("SELECT * FROM traces GROUP BY process_correlation_id") { row->
    processCorelationIds.add(row.process_correlation_id)
}

processCorelationIds.each { id ->
    def f = new File("${traceFileDirectoryName}/process-tracer-${id}.html")

    def tracesMap = [:]
    sql.eachRow("SELECT * FROM traces WHERE process_correlation_id=${id}") { row ->
        tracesMap[row.timestamp] = row.rss_mem_kb
    }

    f.text = ''
    f.append('<html>\n')
    f.append('  <head>\n')
    f.append('      <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>\n')
    f.append('      <script type="text/javascript">\n')
    f.append('          google.charts.load(\'current\', {\'packages\':[\'corechart\']});\n')
    f.append('          google.charts.setOnLoadCallback(drawChart);\n')
    f.append('\n')
    f.append('          function drawChart() {\n')
    f.append('              var data = google.visualization.arrayToDataTable([\n')
    f.append('                  [\'Timestamp\', \'RSS_MEM_KB\'],\n')

    tracesMap.each { timestamp, value ->

        //timeString = new Date((timestamp as long)*1000).format('yyyy:MM:dd:HH:mm:ss').toString()
        //f.append("              ['${timeString}', ${value}],\n")

        //timeList = timeString.tokenize(':')
        f.append("              [new Date(${(timestamp as long) * 1000}), ${value}],\n")
    }

    f.append('              ]);\n')
    f.append('\n')
    f.append('              var options = {\n')
    f.append("                  title: 'RSS Memory in kB for process with correlation id ${id}',\n")
    f.append('                  curveType: \'function\',\n')
    f.append('                  hAxis: {\n')
    f.append('                      format: \'yyyy-MM-dd_HH:mm:ss\',\n')
    f.append('                      direction: -1,\n')
    f.append('                      slantedText: true,\n')
    f.append('                      slantedTextAngle: 60,\n')
    f.append('                  },\n')
    f.append('                  legend: { position: \'bottom\' }\n')
    f.append('              };\n')
    f.append('\n')
    f.append('              var chart = new google.visualization.LineChart(document.getElementById(\'curve_chart\'));\n')
    f.append('\n')
    f.append('              chart.draw(data, options);\n')
    f.append('          }\n')
    f.append('      </script>\n')
    f.append('  </head>\n')
    f.append('  <body>\n')
    f.append('      <div id="curve_chart" style="width: 1000px; height: 500px"></div>\n')
    f.append('  </body>\n')
    f.append('</html>')
}

// create the index.html
def f = new File("${traceFileDirectoryName}/index.html")
f.text=''
f.append('<html>\n')
f.append('  <body>\n')
f.append('      <ul>\n')

processCorelationIds.each{ id->
    f.append("<li><a href=\"./process-tracer-${id}.html\">${id}</a></li>\n")
}

f.append('      </ul>\n')
f.append('  </body>\n')
f.append('</html>\n')