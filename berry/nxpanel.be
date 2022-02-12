
# Sonoff NSPanel Tasmota (Nextion with Flashing) driver | code by peepshow-21
# based on;
# Sonoff NSPanel Tasmota driver v0.47 | code by blakadder and s-hadinger

# Example Flash
# FlashNextion http://172.17.20.5:8080/static/chunks/nxpanel.tft
# FlashNextion http://demo-hadinger.s3.eu-west-3.amazonaws.com:80/nxpanel.tft

class Nextion : Driver

    static VERSION = "v1.1.0-beta2"
    static header = bytes().fromstring("PS")

    static flash_block_size = 4096

    var flash_mode
    var flash_size
    var flash_written
    var flash_buff
    var flash_offset
    var awaiting_offset
    var tcp
    var ser
    var last_per

    def split_msg(b)   
        import string
        var ret = []
        var i = 0
        while i < size(b)-1
            if b[i] == 0x55 && b[i+1] == 0xAA
                if i > 0
                    var nb = b[0..i-1];
                    ret.push(nb)
                end
                b = b[i+2..]
                i = 0
            else
                i+=1
            end
        end
        if size(b) > 0
            ret.push(b)
        end
        return ret
    end

    def crc16(data, poly)
      if !poly  poly = 0xA001 end
      # CRC-16 MODBUS HASHING ALGORITHM
      var crc = 0xFFFF
      for i:0..size(data)-1
        crc = crc ^ data[i]
        for j:0..7
          if crc & 1
            crc = (crc >> 1) ^ poly
          else
            crc = crc >> 1
          end
        end
      end
      return crc
    end

    def encode(payload)
      var b = bytes()
      b += self.header
      var nsp_type = 0 # not used
      b.add(nsp_type)       # add a single byte
      b.add(size(payload), 2)   # add size as 2 bytes, little endian
      b += bytes().fromstring(payload)
      var msg_crc = self.crc16(b)
      b.add(msg_crc, 2)       # crc 2 bytes, little endian
      return b
    end

    def encodenx(payload)
        var b = bytes().fromstring(payload)
        b += bytes('FFFFFF')
        return b
    end

    def sendnx(payload)
        import string
        var payload_bin = self.encodenx(payload)
        self.ser.write(payload_bin)
        log(string.format("NXP: Nextion command sent = %s",str(payload_bin)), 3)       
    end

    def send(payload)
        var payload_bin = self.encode(payload)
        if self.flash_mode==1
            log("NXP: skipped command becuase still flashing", 3)
        else 
            self.ser.write(payload_bin)
            log("NXP: payload sent = " + str(payload_bin), 3)
        end
    end

    def write_to_nextion(b)
        self.ser.write(b)
    end

    def init()
        log("NXP: Initializing Driver")
        self.ser = serial(17, 16, 115200, serial.SERIAL_8N1)
        self.sendnx('DRAKJHSUYDGBNCJHGJKSHBDN')
        self.flash_mode = 0
    end

    def screeninit()
        log("NXP: Screen Initialized") 
        self.sendnx("berry_ver.txt=\"berry: "+self.VERSION+"\"")
    end

    def write_block()
        
        import string
        log("FLH: Read block",3)
        while size(self.flash_buff)<self.flash_block_size && self.tcp.connected()
            if self.tcp.available()>0
                self.flash_buff += self.tcp.readbytes()
            else
                tasmota.delay(50)
                log("FLH: Wait for available...",3)
            end
        end
        log("FLH: Buff size "+str(size(self.flash_buff)),3)
        var to_write
        if size(self.flash_buff)>self.flash_block_size
            to_write = self.flash_buff[0..self.flash_block_size-1]
            self.flash_buff = self.flash_buff[self.flash_block_size..]
        else
            to_write = self.flash_buff
            self.flash_buff = bytes()
        end
        log("FLH: Writing "+str(size(to_write)),3)
        var per = (self.flash_written*100)/self.flash_size
        if (self.last_per!=per) 
            self.last_per = per
            tasmota.publish_result(string.format("{\"Flashing\":{\"complete\": %d}}",per), "RESULT") 
        end
        if size(to_write)>0
            self.flash_written += size(to_write)
            if self.flash_offset==0 || self.flash_written>self.flash_offset
                self.ser.write(to_write)
                self.flash_offset = 0
            else
                tasmota.set_timer(10,/->self.write_block())
            end
        end
        log("FLH: Total "+str(self.flash_written),3)
        if (self.flash_written==self.flash_size)
            log("FLH: Flashing complete")
            self.flash_mode = 0
        end

    end

    def every_100ms()
        import string
        if self.ser.available() > 0
            var msg = self.ser.read()
            if size(msg) > 0
                log(string.format("NXP: Received Raw = %s",str(msg)), 3)
                if (self.flash_mode==1)
                    var strv = msg[0..-4].asstring()
                    if string.find(strv,"comok 2")>=0
                        log("FLH: Send (High Speed) flash start")
                        self.sendnx(string.format("whmi-wris %d,115200,res0",self.flash_size))
                    elif size(msg)==1 && msg[0]==0x08
                        log("FLH: Waiting offset...",3)
                        self.awaiting_offset = 1
                    elif size(msg)==4 && self.awaiting_offset==1
                        self.awaiting_offset = 0
                        self.flash_offset = msg.get(0,4)
                        log("FLH: Flash offset marker "+str(self.flash_offset),3)
                        self.write_block()
                    elif size(msg)==1 && msg[0]==0x05
                        self.write_block()
                    else
                        log("FLH: Something has gone wrong flashing nxpanel ["+str(msg)+"]",2)
                    end
                else
                    var msg_list = self.split_msg(msg)
                    for i:0..size(msg_list)-1
                        msg = msg_list[i]
                        if size(msg) > 0
                            if msg == bytes('000000FFFFFF88FFFFFF')
                                self.screeninit()
                            elif msg[0]==0x7B # JSON, starting with "{"
                                var jm = string.format("%s",msg[0..-1].asstring())
                                tasmota.publish_result(jm, "RESULT")        
                            elif msg[0]==0x07 && size(msg)==1 # BELL/Buzzer
                                tasmota.cmd("buzzer 1,1")
                            else
                                var jm = string.format("{\"nextion\":\"%s\"}",str(msg[0..-4]))
                                tasmota.publish_result(jm, "RESULT")        
                            end
                        end       
                    end
                end
            end
        end
    end      

    def begin_nextion_flash()
        self.flash_written = 0
        self.awaiting_offset = 0
        self.flash_offset = 0
        self.sendnx('DRAKJHSUYDGBNCJHGJKSHBDN')
        self.sendnx('recmod=0')
        self.sendnx('recmod=0')
        self.flash_mode = 1
        self.sendnx("connect")        
    end
    
    def set_power()
      var ps = tasmota.get_power()
      for i:0..1
        if ps[i] == true
          ps[i] = "1"
        else 
          ps[i] = "0"
        end
      end
      var json_payload = '{ "switches": { "switch1": ' + ps[0] + ' , "switch2": ' + ps[1] +  ' } }'
      log('NXP: Switch state updated with ' + json_payload)
      self.send(json_payload)
    end

    def set_clock()
      var now = tasmota.rtc()
      var time_raw = now['local']
      var nsp_time = tasmota.time_dump(time_raw)
      var time_payload = '{ "clock": { "date":' + str(nsp_time['day']) + ',"month":' + str(nsp_time['month']) + ',"year":' + str(nsp_time['year']) + ',"weekday":' + str(nsp_time['weekday']) + ',"hour":' + str(nsp_time['hour']) + ',"min":' + str(nsp_time['min']) + ' } }'
      log('NXP: Time and date synced with ' + time_payload, 3)
      self.send(time_payload)
    end

    def open_url(url)

        import string
        var host
        var port
        var s1 = string.split(url,7)[1]
        var i = string.find(s1,":")
        var sa
        if i<0
            port = 80
            i = string.find(s1,"/")
            sa = string.split(s1,i)
            host = sa[0]
        else
            sa = string.split(s1,i)
            host = sa[0]
            s1 = string.split(sa[1],1)[1]
            i = string.find(s1,"/")
            sa = string.split(s1,i)
            port = int(sa[0])
        end
        var get = sa[1]
        log(string.format("FLH: host: %s, port: %s, get: %s",host,port,get))
        self.tcp = tcpclient()
        self.tcp.connect(host,port)
        log("FLH: Connected:"+str(self.tcp.connected()),3)
        var get_req = "GET "+url+" HTTP/1.0\r\n\r\n"
        self.tcp.write(get_req)
        var a = self.tcp.available()
        i = 0
        while a==0 && i<3
          tasmota.delay(100)
          i += 1
          log("FLH: Retry "+str(i),3)
          a = self.tcp.available()
        end
        if a==0
            return
        end
        var b = self.tcp.readbytes()
        i = 0
        var end_headers = false;
        var headers
        while i<size(b) && headers==nil
            if b[i..(i+3)]==bytes().fromstring("\r\n\r\n") 
                headers = b[0..(i+3)].asstring()
                self.flash_buff = b[(i+4)..]
            else
                i += 1
            end
        end
        #print(headers)
        var tag = "Content-Length: "
        i = string.find(headers,tag)
        if (i>0) 
            var i2 = string.find(headers,"\r\n",i)
            var s = headers[i+size(tag)..i2-1]
            self.flash_size=int(s)
        end
        if self.flash_size==0
            log("FLH: No size header, counting ...",3)
            self.flash_size = size(self.flash_buff)
            #print("counting start ...")
            while self.tcp.connected()
                while self.tcp.available()>0
                    self.flash_size += size(self.tcp.readbytes())
                end
                tasmota.delay(50)
            end
            #print("counting end ...",self.flash_size)
            self.tcp.close()
            self.open_url(url)
        else
            log("FLH: Size found in header, skip count",3)
        end
        log("FLH: Flash file size: "+str(self.flash_size),3)

    end

    def flash_nextion(url)

        self.flash_size = 0
        self.open_url(url)
        self.begin_nextion_flash()

    end

end

var nextion = Nextion()

tasmota.add_driver(nextion)

def flash_nextion(cmd, idx, payload, payload_json)
    def task()
        nextion.flash_nextion(payload)
    end
    tasmota.set_timer(0,task)
    tasmota.resp_cmnd_done()
end

tasmota.add_cmd('FlashNextion', flash_nextion)

def send_cmd(cmd, idx, payload, payload_json)
    nextion.sendnx(payload)
    tasmota.resp_cmnd_done()
end

tasmota.add_cmd('Nextion', send_cmd)

def send_cmd2(cmd, idx, payload, payload_json)
    nextion.send(payload)
    tasmota.resp_cmnd_done()
end

tasmota.add_cmd('Screen', send_cmd2)
tasmota.add_cmd('NxPanel', send_cmd2)

tasmota.add_rule("power1#state", /-> nextion.set_power())
tasmota.add_rule("power2#state", /-> nextion.set_power())
tasmota.cmd("Rule3 1") # needed until Berry bug fixed
tasmota.add_rule("Time#Minute", /-> nextion.set_clock())
tasmota.cmd("State")

