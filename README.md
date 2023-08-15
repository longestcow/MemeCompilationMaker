# Meme Compilation Maker
#### Description: 
I was scrolling through Youtube and found a [hollow knight meme compilation video](https://youtu.be/2YiZtwzOR3Q) and realised that this entire process could be automated -  
* Get top 100 posts from past week from some meme subreddit  
* Make a video out of these images using ffmpeg  
* put some copyright free music on top with ffmpeg 
* upload the video using youtube api (waiting for google to give me api usage permission) 

For the first step, I used Reddit's JSON api which allows me to get JSON objects of the posts.  
For example, if I wanted the top 100 posts from past week from a subreddit called "wholesomememes", I would use the following link:  
https://www.reddit.com/r/wholesomememes/top/?t=week&limit=100  
Now if I wanted these posts in JSON format, I would use this link:  
https://www.reddit.com/r/wholesomememes/top.json?t=week&limit=100  
This link returns the posts as JSON objects, which I can then fetch and read through Java code using the GSON library.  
All of this is done so that I can go over all the posts and get the image URLS to put into a list.  
I still cant just use every single post since some memes are videos or gifs or just text posts, so I filter out whatever I can't use, retaining only the posts with .jpg or .png images.  
  
After this entire process of getting all the image urls into a list, I then go over the list and download every image onto my device temporarily.  
This is done using the Java ImageIO class. All of the images get downloaded to a folder called "memes".  
Downloaidng all of them to my device is necessary since FFMPEG needs to access these images in order to work with them.  
  
Once the images are downloaded, I use [FFMPEG](https://www.ffmpeg.org) (which is an awesome tool for processing and working with video and audio files) to concatenate all of the images into one video. I found the [documentation](https://trac.ffmpeg.org/wiki/Concatenate) very useful.  
Once I have this video, I randomly pick out a music file out of 9 or so copyright free music files that I have downloaded on my device.  
FFMPEG is used again here to combine the compilation video with this selected music file.  
After this process is done and the new video is created, the old video file without the music is deleted.  
You can find this new file in the "vids" folder. After all of that, I simply take this video file and upload it to Youtube.  
As I said above, I have plans on automating the final step as well. I'm waiting for google to give me the permission for the Youtube API usage.    
Another feature I had in mind was to make a video-meme version where instead of getting static images from a subreddit, it instead gets videos (mp4s, mkvs, etc) and puts them all together. I have [written out the foundation](https://github.com/longestcow/MemeCompilationMaker/blob/main/src/VidMemesUploader.java) for this but it is still somewhat buggy.  
[The main code file](https://github.com/longestcow/MemeCompilationMaker/blob/main/src/MemesUploader.java).  
I enjoyed making this and understanding how ffmpeg and other libraries like GSON work and I plan on adding more functionalities and eventually automating the process entirely.  

  

  
