#!/usr/bin/python

# Database Application Prototype by Guilherme Borges
# 2016 DI-FCT-UNL
#
# To change connection details check the main function.
# This program assumes two MySQL databases, 'music_enc' and 'music_unenc', are already created.
# To run in unencrypted mode, add '-u' option.
import sys
import time
import base64
import socket, ssl, errno, json
from texttable import Texttable

TCP_IP = '127.0.0.1' #proxy ip
TCP_PORT = 5482

file_stat = None

# Sends a query to the proxy with a socket
def send_query(query):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(60)
        
        # WRAP SOCKET
        #s = ssl.wrap_socket(s)
        
        s.connect((TCP_IP, TCP_PORT))
        s.send(str(len(query)).zfill(10) + query)

        data = s.recv(10)   # first we receive the size of the message in bytes
                            # size always takes up 10 bytes

        size = int(data)
        received = 0
        data = ""
        
        while(received < size):
            tmp = s.recv(size)
            received += len(tmp)
            data += tmp
        
        #print str(len(query)).zfill(10) + " " + str(size)
        
        s.close()
        return data
    except socket.timeout:
        print("Timeout: no answer from proxy")

    except socket.error as e:
        if e.errno == errno.ECONNREFUSED:
            print("Connection refused on %s:%s" % (TCP_IP, TCP_PORT))
        elif e.errno == errno.ECONNRESET:
            print("Connection reset by peer")
        else:
            print("Unexpected exception: " '''+ e.num''')
            
    return None
    
def query_db(query):
    result = send_query(query)
    if(query == None):
        return None
    else:
        return json.loads(result)
            
##########        
def create_tables():
    query_db("DROP TABLE IF EXISTS album")
    query_db("""CREATE TABLE album (                        
                        title VARCHAR(25) NOT NULL,
                        artist VARCHAR(20) NOT NULL,
                        year INT(4) UNSIGNED,
                        value INT(5) UNSIGNED,
                        CONSTRAINT unq_alb UNIQUE (title, artist)
                    )""")

    query_db("DROP TABLE IF EXISTS music")
    query_db("""CREATE TABLE music (
                        title VARCHAR(25) NOT NULL,
                        artist VARCHAR(20) NOT NULL,
                        album VARCHAR(20),
                        duration INT(6))""")
    
    query_db("DROP TABLE IF EXISTS images")
    query_db("""CREATE TABLE images (  
                id INT(4) NOT NULL PRIMARY KEY,
                name VARCHAR(20),
                tags VARCHAR(700),                      
                hash VARCHAR(512) NOT NULL
            )""")

##########
def print_tbl(data, header):
    table = Texttable()
    halign, valign = [], []
    
    for el in header:
        halign.append("c")
        valign.append("m")
        
    table.set_cols_align(halign)
    table.set_cols_valign(valign)

    table.add_row(header)
    for row in data:
        table.add_row([str(i) for i in list(row)])

    print(table.draw() + "\n")

##########
def count_table(tbl):
    tmp = query_db("SELECT COUNT(*) FROM " + tbl)
    count = int(tmp["row 1"][0])
    return count

##########    
def insert(op):
    if op == 'm':
        msg1    = "\tINSERT SONGS"
        typel    = "music"
        m_query    = "INSERT INTO music (title,artist,album,duration) VALUES ('%s','%s','%s',%d)"
        msg2    = "\tInserted %d song(s)"
    else:
        msg1    = "\tINSERT ALBUMS"
        typel    = "album"
        m_query    = "INSERT INTO album (title,artist,year,value) VALUES ('%s','%s',%d,%d)"
        msg2    = "\tInserted %d album(s)"
        
    print(msg1)
    count = count_table(typel)
    
    total_time = 0
    #loop to insert data
    while True:
        cmd = raw_input('\t> ').split()
        
        #length is right
        if len(cmd) == 4:
            query = m_query % (cmd[0], cmd[1], cmd[2] if op == 'm' else int(cmd[2]), int(cmd[3]))
            try:
                start = time.time()
                query_db(query)
                total_time += time.time() - start
                query_db("COMMIT")
            except:
                print("Something went wrong")
        elif len(cmd) == 1 and '.' in cmd[0]:
            break
        else:
            print("Wrong arguments")

    dif = count_table(typel) - count
    if dif > 0:
        print(msg2 % (dif))
        print("\tAvg duration of inserts: %f ms" % (total_time/dif * 1000))
        file_stat.write("INSERT\t\t\t%s\t\t%s\n" % (dif, total_time*1000))
        
##########
def consult(op):
    if op == 'm':
        query    = "SELECT title,artist,album,duration FROM music"
        msg1    = "\tLIST OF SONGS..."
        header    = ["Title", "Artist", "Album", "Duration (s)"]
    else:
        query    = "SELECT title, artist, year, value FROM album"
        msg1    = "\tLIST OF ALBUMS..."
        header    = ["Title", "Artist", "Year", "Value"]
    
    print(msg1)
    
    start = time.time()
    
    data = query_db(query)
    
    tme = (time.time() - start) * 1000
    count = data["rowcount"]
    
    tmp = []
    for i in range(1, count+1):
        tmp.append(data["row " + str(i)])
    
    print_tbl(tmp, header)
    print("Duration of select: %s ms" % tme)
    file_stat.write("SELECT_SIMPLE\t%s\t\t%s\n" % (count,tme))
    
##########
def calc_duration():
    cmd = raw_input('\tAll albums? (y/n) ')
    if 'n' in cmd:
        alb = raw_input('\tAlbum name? ')
        art = raw_input('\tArtist? ')
        query = "SELECT SUM(duration) FROM music WHERE album='%s' AND artist='%s'" % (alb, art)
    else:
        query = ("SELECT SUM(duration) FROM music")
    
    start = time.time()
    data = query_db(query)
    
    tme = (time.time() - start) * 1000
    count = data["rowcount"]
    
    #print("Total duration of songs is %s seconds.\n" % (int(data["row 1"][0])))
    file_stat.write(("SELECT_EQ" if 'n' in cmd else "SELECT_SUM") + "\t\t%s\t\t%s\n" % (count,tme))

##########
def order_value():
    cmd = raw_input('\tAsc or desc? (a/d) ')
    
    if 'a' in cmd:
        query = "SELECT title,artist,year,value FROM album ORDER BY value ASC"
    else:
        query = "SELECT title,artist,year,value FROM album ORDER BY value DESC"
    
    start = time.time()
    
    tmp = query_db(query)
    
    tme = (time.time() - start) * 1000
    count = tmp["rowcount"]
    
    print("\tORDER ALBUMS " + "ASC" if 'a' in cmd else "DESC")
    
    data = []
    for i in range(1, count + 1):
        data.append(tmp["row " + str(i)])
    
    print_tbl(data, ["Title", "Artist", "Year", "Value"])
    file_stat.write("SELECT_ORDER_%s\t%s\t\t%s\n" % ("A" if 'a' in cmd else "D", count, tme))
    
##########    
def increment_val():
    print("\tIncrementing all albums' value by 10")
    start = time.time()
    
    data = query_db("UPDATE album SET value=value+10")
    
    count = data["rowcount"]
    tme = (time.time() - start) * 1000
    query_db("COMMIT")
    print("\n Operation of update on %s rows took %s ms" % (count, tme))
    file_stat.write("UPDT_INC\t\t%s\t\t%s\n" % (count,tme))
    
##########    
def join_album_song():
    print("Joining songs and albums from same artist")

    start = time.time()
    
    tmp = query_db("SELECT music.title,music.album,music.artist,album.year FROM album INNER JOIN music USING (artist)")
    
    tme = (time.time() - start) * 1000
    count = tmp["rowcount"]
    
    data = []
    for i in range(1, count + 1):
        data.append(tmp["row " + str(i)])
    
    print_tbl(data, ["Title", "Album", "Artist", "Year"])
    print("\n Operation of join on %s rows took %s ms" % (count, tme))
    file_stat.write("SELECT_JOIN\t\t%s\t\t%s\n" % (count,tme))

##########
def search_by_keyword_or_img():
    cmd1 = raw_input('\tPhoto id? ') #/home/pepper/apdc/datasets/flickr_imgs/im64.jpg
    cmd1 = "/home/pepper/apdc/datasets/flickr_imgs/im" + cmd1 + ".jpg"
    
    cmd2 = raw_input('\tTag(s)? ')
    
    with open(cmd1, "rb") as img_file:
        img = base64.b64encode(img_file.read())
        
    start = time.time()
    res = query_db("SELECT * FROM images WHERE tag='%s' OR IMAGE ~<%s>" % (cmd2, img))
    tme = (time.time() - start) * 1000
    
    i = 0
    while i < res["rowcount"]:
        print("Got image " + res["row_id_" + str(i)] + " with score " + res["row_score_" + str(i)])
        #save retrieved images to program's folder
        with open(res["row_id_" + str(i)] + ".jpg", "wb") as img_file:
            img_file.write(base64.b64decode(res["row_image_" + str(i)]))
        
        i = i + 1
    
    file_stat.write(("IMG_QUERY") + "\t\t%s\t\t%s\n" % (i,tme))

##########
def sql_query():
    query = raw_input("Insert query: ")

    start = time.time()    
    res = query_db(query)
    tme = (time.time() - start) * 1000
    
    print("Proxy answered in "+str(tme)+"ms:\n" + str(res) + "\n")
    file_stat.write(("MANUAL_QUERY") + "\t\t%s\t\t%s\n%s\n" % ("1",tme,query))
        
##########
def clean_tables():
    cmd = raw_input('\tDelete all data? (y/n) ')
    
    if 'y' in cmd:
        query_db("DELETE FROM album")
        query_db("DELETE FROM music")
        query_db("DELETE FROM images")
        query_db("COMMIT")
        print("\tTables cleaned")
        
def login():
    res = query_db("META LOGIN guilherme apdc_2016")
    
    if(res["success"]):
        print("Logged in")
    else:
        print("Error logging in, untrustable server...")
        sys.exit(0)

##########
def main():
    print('<<<<---- Music Collection Manager ---->>>>')
    print("\tConnecting to enhanced proxy...")
    encrypt = True
    cloud = True
    
    print("Login to server")
    res = query_db("META LOGIN guilherme apdc_2016")
    
    #get status
    print("Get status from server")
    res = query_db("META STATUS")
    if(not res["isEncrypted"]):
        encrypt = False
    if(not res["isCloud"]):
        cloud = False
        
    print("\tStatus: encrypt %s; cloud %s" % (str(encrypt), str(cloud)))

    #check if tables are created
    res = query_db("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'music_enc'")
    if(res["rowcount"] < 2):
        print("\tCreating tables...\n")
        create_tables()
    else:
        print("\tExisting tables fetched...\n")

    #open file for outputs
    name = 'stats_' + str(int(time.time())) + ('_enc' if encrypt else '_unenc') + ('_rem' if cloud else '_loc')
    global file_stat
    file_stat = open(name, 'w')
    print('Opening file %s') % name
    
    login()
    
    #main loop
    while True:
        cmd = raw_input('\n> ').split()

        if(len(cmd) == 0):
            continue
            
        elif(cmd[0][0] == '%'):
            continue
            
        elif(cmd[0] == 'fname'):
            file_stat.write(cmd[1] + "\n")

        elif(cmd[0] == 'img' or cmd[0] == 'i'):
            search_by_keyword_or_img() 
                
        elif(cmd[0] == 'consult' or cmd[0] == 'cons'):
            if len(cmd) != 2:
                print("\tMissing arg 'm' or 'a'")
                continue
                
            if cmd[1] == 'm' :
                consult('m')
            else:
                consult('a')
                
        elif(cmd[0] == 'sql'):
            sql_query()
        elif(cmd[0] == 'insert' or cmd[0] == 'ins'):
            if len(cmd) != 2:
                print("\tMissing arg 'm' or 'a'")
                continue
                
            if cmd[1] == 'm' :
                insert('m')
            else:
                insert('a')
                
        elif(cmd[0] == 'duration' or cmd[0] == 'dur'):
            calc_duration()
            
        elif(cmd[0] == 'order' or cmd[0] == 'ord'):
            order_value()
            
        elif(cmd[0] == 'increment' or cmd[0] == 'inc'):
            increment_val()
            
        elif(cmd[0] == 'join' or cmd[0] == 'j'):
            join_album_song()

        elif(cmd[0] == 'clean'):
            clean_tables()
            
        elif(cmd[0] == 'exit' or cmd[0] == 'e'):
            break
            
        else:
            print("??")
            
    print("Closing...")
    file_stat.close()
    query_db("META LOGOUT")

try:
    main()
except KeyboardInterrupt:
    query_db("META LOGOUT")
    print('\nExiting...\n')
    sys.exit(0)
